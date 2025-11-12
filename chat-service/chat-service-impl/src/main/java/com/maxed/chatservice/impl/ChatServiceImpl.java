package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.*;
import com.maxed.userservice.impl.User;
import com.maxed.userservice.impl.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request) {
        User currentUser = getCurrentUser();
        User partner = userRepository.findById(request.partnerId()).orElseThrow(() -> new RuntimeException("User not found"));

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
        User currentUser = getCurrentUser();
        return chatRepository.findChatsByParticipantId(currentUser.getId()).stream()
                .map(this::toChatResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId) {
        User currentUser = getCurrentUser();
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());
        return toChatResponse(chat);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request) {
        User currentUser = getCurrentUser();
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
        User currentUser = getCurrentUser();
        findAndVerifyChatParticipant(chatId, currentUser.getId());
        return messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable)
                .map(this::toMessageResponse);
    }

    private Chat findAndVerifyChatParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (chat.getParticipants().stream().noneMatch(user -> user.getId().equals(userId))) {
            throw new RuntimeException("User not part of this chat"); // Should be a 403 Forbidden
        }
        return chat;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
