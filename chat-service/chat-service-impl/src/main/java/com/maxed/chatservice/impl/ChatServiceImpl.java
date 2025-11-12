package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.ChatResponse;
import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.ChatType;
import com.maxed.chatservice.api.CreateDirectChatRequest;
import com.maxed.chatservice.api.MessageResponse;
import com.maxed.chatservice.api.SendMessageRequest;
import com.maxed.chatservice.api.UserSummaryResponse;
import com.maxed.chatservice.api.exception.ForbiddenException;
import com.maxed.chatservice.api.exception.ResourceNotFoundException;
import com.maxed.userservice.api.IAuthenticationFacade;
import com.maxed.userservice.impl.User;
import com.maxed.userservice.impl.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

    private User getAuthenticatedUser() {
        UserDetails userDetails = authenticationFacade.getAuthenticatedUser();
        if (userDetails instanceof User) {
            return (User) userDetails;
        }
        throw new IllegalStateException("Authenticated user is not an instance of com.maxed.userservice.impl.User");
    }

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request) {
        User currentUser = getAuthenticatedUser();
        User partner = userRepository.findById(request.partnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.partnerId()));

        return chatRepository.findDirectChatBetweenUsers(currentUser.getId(), partner.getId())
                .map(this::toChatResponse)
                .orElseGet(() -> {
                    Chat newChat = new Chat();
                    newChat.setType(ChatType.DIRECT);
                    Set<User> participants = new HashSet<>();
                    participants.add(currentUser);
                    participants.add(partner);
                    newChat.setParticipants(participants);
                    return toChatResponse(chatRepository.save(newChat));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        List<Object[]> results = chatRepository.findChatsAndLatestMessageByParticipantId(currentUser.getId());

        return results.stream().map(result -> {
            Chat chat = (Chat) result[0];
            Message latestMessage = (Message) result[1];
            return toChatResponse(chat, latestMessage);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId) {
        User currentUser = getAuthenticatedUser();
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());
        return toChatResponse(chat);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request) {
        User currentUser = getAuthenticatedUser();
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
    public Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable) {
        User currentUser = getAuthenticatedUser();
        findAndVerifyChatParticipant(chatId, currentUser.getId());
        return messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable)
                .map(this::toMessageResponse);
    }

    private Chat findAndVerifyChatParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
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
