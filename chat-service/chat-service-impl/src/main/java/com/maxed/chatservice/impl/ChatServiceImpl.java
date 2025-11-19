package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.*;
import com.maxed.chatservice.api.exception.ForbiddenException;
import com.maxed.chatservice.api.exception.ResourceNotFoundException;
import com.maxed.userservice.api.User;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request, User currentUser) {
        UserResponse partner = userService.getUserById(request.partnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.partnerId()));

        Optional<Chat> existingChat = chatRepository.findDirectChatBetweenUsers(currentUser.getId(), partner.id());

        if (existingChat.isPresent()) {
            return toChatResponse(existingChat.get(), currentUser, partner);
        }

        Chat newChat = Chat.builder()
                .type(ChatType.DIRECT)
                .participantIds(Set.of(currentUser.getId(), partner.id()))
                .name(partner.username())
                .build();

        return toChatResponse(chatRepository.save(newChat), currentUser, partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForCurrentUser(User currentUser) {
        List<Chat> chats = chatRepository.findChatsByParticipantId(currentUser.getId());

        Set<Long> allUserIds = chats.stream()
                .flatMap(chat -> chat.getParticipantIds().stream())
                .collect(Collectors.toSet());

        Map<Long, UserResponse> userMap = getUsersMap(allUserIds);

        return chats.stream().map(chat -> {
            Message latestMessage = messageRepository.findFirstByChatIdOrderByTimestampDesc(chat.getId()).orElse(null);
            return buildChatResponse(chat, latestMessage, userMap);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request, User currentUser) {
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());

        Message message = Message.builder()
                .content(request.content())
                .authorId(currentUser.getId())
                .chat(chat)
                .build();

        Message savedMsg = messageRepository.save(message);

        UserSummaryResponse authorSummary = new UserSummaryResponse(currentUser.getId(), currentUser.getUsername());
        return new MessageResponse(savedMsg.getId(), savedMsg.getContent(), savedMsg.getTimestamp(), authorSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId, User currentUser) {
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());

        Map<Long, UserResponse> userMap = getUsersMap(chat.getParticipantIds());

        Message latestMessage = messageRepository.findFirstByChatIdOrderByTimestampDesc(chatId).orElse(null);
        return buildChatResponse(chat, latestMessage, userMap);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable, User currentUser) {
        findAndVerifyChatParticipant(chatId, currentUser.getId());

        Page<Message> messages = messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable);

        Set<Long> authorIds = messages.stream().map(Message::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserResponse> authorsMap = getUsersMap(authorIds);

        return messages.map(msg -> {
            UserResponse author = authorsMap.get(msg.getAuthorId());
            UserSummaryResponse userSummary = (author != null)
                    ? new UserSummaryResponse(author.id(), author.username())
                    : new UserSummaryResponse(msg.getAuthorId(), "Unknown");

            return new MessageResponse(msg.getId(), msg.getContent(), msg.getTimestamp(), userSummary);
        });
    }

    private Chat findAndVerifyChatParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with ID: " + chatId));

        if (!chat.getParticipantIds().contains(userId)) {
            throw new ForbiddenException("User is not a participant of chat with ID: " + chatId);
        }
        return chat;
    }

    private Map<Long, UserResponse> getUsersMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserResponse::id, Function.identity()));
    }

    private ChatResponse toChatResponse(Chat chat, User currentUser, UserResponse partner) {
        Set<UserSummaryResponse> participants = Set.of(
                new UserSummaryResponse(currentUser.getId(), currentUser.getUsername()),
                new UserSummaryResponse(partner.id(), partner.username())
        );
        return new ChatResponse(chat.getId(), chat.getName(), chat.getType(), participants, null);
    }

    private ChatResponse buildChatResponse(Chat chat, Message latestMsg, Map<Long, UserResponse> userMap) {
        Set<UserSummaryResponse> participants = chat.getParticipantIds().stream()
                .map(id -> {
                    UserResponse u = userMap.get(id);
                    return u != null ? new UserSummaryResponse(u.id(), u.username()) : new UserSummaryResponse(id, "Unknown");
                })
                .collect(Collectors.toSet());

        MessageResponse msgResponse = null;
        if (latestMsg != null) {
            UserResponse author = userMap.get(latestMsg.getAuthorId());
            UserSummaryResponse authorSummary = (author != null)
                    ? new UserSummaryResponse(author.id(), author.username())
                    : new UserSummaryResponse(latestMsg.getAuthorId(), "Unknown");
            msgResponse = new MessageResponse(latestMsg.getId(), latestMsg.getContent(), latestMsg.getTimestamp(), authorSummary);
        }

        return new ChatResponse(chat.getId(), chat.getName(), chat.getType(), participants, msgResponse);
    }
}
