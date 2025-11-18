package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.exception.ForbiddenException;
import com.maxed.chatservice.api.exception.ResourceNotFoundException;
import com.maxed.userservice.api.IAuthenticationFacade;
import com.maxed.chatservice.api.*;
import com.maxed.userservice.impl.User;
import com.maxed.userservice.impl.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final IAuthenticationFacade authenticationFacade;

    private User getAuthenticatedUserEntity() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        return getUserFromPrincipal(principal);
    }

    private User getUserFromPrincipal(Object principal) {
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            username = ((Principal) principal).getName();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Authenticated user principal is not an instance of UserDetails, Principal, or String, but: " + principal.getClass().getName());
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database: " + username));
    }

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request) {
        User currentUser = getAuthenticatedUserEntity();
        User partner = userRepository.findById(request.partnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.partnerId()));

        return chatRepository.findDirectChatBetweenUsers(currentUser.getId(), partner.getId())
                .map(this::toChatResponse)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .type(ChatType.DIRECT)
                            .participants(Set.of(currentUser, partner))
                            .build();
                    return toChatResponse(chatRepository.save(newChat));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForCurrentUser() {
        User currentUser = getAuthenticatedUserEntity();
        List<Object[]> results = chatRepository.findChatsAndLatestMessageByParticipantId(currentUser.getId());

        return results.stream().map(result -> {
            Chat chat = (Chat) result[0];
            Message latestMessage = (Message) result[1];
            return toChatResponse(chat, latestMessage);
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());

        Message message = Message.builder()
                .content(request.content())
                .author(currentUser)
                .chat(chat)
                .build();

        return toMessageResponse(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId) {
        User currentUser = getAuthenticatedUserEntity();
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());
        return toChatResponse(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable) {
        User currentUser = getAuthenticatedUserEntity();
        findAndVerifyChatParticipant(chatId, currentUser.getId());
        return messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable)
                .map(this::toMessageResponse);
    }

    private Chat findAndVerifyChatParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findByIdWithParticipants(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with ID: " + chatId));
        if (chat.getParticipants().stream().noneMatch(user -> user.getId().equals(userId))) {
            throw new ForbiddenException("User is not a participant of chat with ID: " + chatId);
        }
        return chat;
    }

    private MessageResponse toMessageResponse(Message message) {
        if (message == null) return null;
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getTimestamp(),
                new UserSummaryResponse(message.getAuthor().getId(), message.getAuthor().getUsername())
        );
    }

    private ChatResponse toChatResponse(Chat chat, Message latestMessage) {
        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                chat.getParticipants().stream()
                        .map(user -> new UserSummaryResponse(user.getId(), user.getUsername()))
                        .collect(Collectors.toSet()),
                toMessageResponse(latestMessage)
        );
    }

    private ChatResponse toChatResponse(Chat chat) {
        MessageResponse latestMessage = messageRepository.findFirstByChatIdOrderByTimestampDesc(chat.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                chat.getParticipants().stream()
                        .map(user -> new UserSummaryResponse(user.getId(), user.getUsername()))
                        .collect(Collectors.toSet()),
                latestMessage
        );
    }
}
