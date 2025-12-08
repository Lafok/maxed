package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.*;
import com.maxed.chatservice.api.exception.ForbiddenException;
import com.maxed.chatservice.api.exception.ResourceNotFoundException;
import com.maxed.chatservice.impl.presence.PresenceService;
import com.maxed.common.event.MessageCreatedEvent;
import com.maxed.mediaservice.api.MediaService;
import com.maxed.userservice.api.User;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final PresenceService presenceService;
    private final MediaService mediaService;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request, User currentUser) {
        UserResponse partner = userService.getUserById(request.partnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.partnerId()));

        Optional<Chat> existingChat = chatRepository.findDirectChatBetweenUsers(currentUser.getId(), partner.id());

        if (existingChat.isPresent()) {
            Map<Long, Boolean> onlineMap = presenceService.getUsersOnlineStatus(
                    Set.of(currentUser.getId(), partner.id())
            );
            return toChatResponse(existingChat.get(), currentUser, partner, onlineMap);
        }

        Chat newChat = Chat.builder()
                .type(ChatType.DIRECT)
                .participantIds(Set.of(currentUser.getId(), partner.id()))
                .name(partner.username())
                .build();

        Chat savedChat = chatRepository.save(newChat);

        Map<Long, Boolean> onlineMapForCreator = presenceService.getUsersOnlineStatus(Set.of(partner.id()));
        onlineMapForCreator.put(currentUser.getId(), true);
        ChatResponse responseForCreator = toChatResponse(savedChat, currentUser, partner, onlineMapForCreator);

        Map<Long, Boolean> onlineMapForPartner = new HashMap<>();
        onlineMapForPartner.put(partner.id(), onlineMapForCreator.getOrDefault(partner.id(), false));
        onlineMapForPartner.put(currentUser.getId(), true);

        ChatResponse responseForPartner = toChatResponse(savedChat, currentUser, partner, onlineMapForPartner);

        messagingTemplate.convertAndSend("/topic/users." + partner.username() + ".chats", responseForPartner);

        return responseForCreator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForCurrentUser(User currentUser) {
        List<Chat> chats = chatRepository.findChatsByParticipantId(currentUser.getId());

        Set<Long> allUserIds = chats.stream()
                .flatMap(chat -> chat.getParticipantIds().stream())
                .collect(Collectors.toSet());

        Map<Long, UserResponse> userMap = getUsersMap(allUserIds);
        Map<Long, Boolean> onlineMap = presenceService.getUsersOnlineStatus(allUserIds);

        return chats.stream().map(chat -> {
            Message latestMessage = messageRepository.findFirstByChatIdOrderByTimestampDesc(chat.getId()).orElse(null);
            return buildChatResponse(chat, latestMessage, userMap, onlineMap);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request, User currentUser) {
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());

        MessageType msgType = request.type() != null ? request.type() : MessageType.TEXT;

        Message message = Message.builder()
                .content(request.content())
                .type(msgType)
                .authorId(currentUser.getId())
                .chat(chat)
                .build();

        Message savedMsg = messageRepository.save(message);

        var event = new MessageCreatedEvent(
                String.valueOf(savedMsg.getId()),
                savedMsg.getContent(),
                chatId,
                currentUser.getId(),
                savedMsg.getTimestamp(),
                savedMsg.getType()
        );
        kafkaTemplate.send("chat.messages", String.valueOf(chatId), event);


        UserSummaryResponse authorSummary = new UserSummaryResponse(
                currentUser.getId(),
                currentUser.getUsername(),
                true
        );

        String contentUrl = (savedMsg.getType() == MessageType.TEXT)
                ? savedMsg.getContent()
                : mediaService.getPresignedUrl(savedMsg.getContent());

        return new MessageResponse(
                savedMsg.getId(),
                contentUrl,
                savedMsg.getType(),
                savedMsg.getTimestamp(),
                authorSummary
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId, User currentUser) {
        Chat chat = findAndVerifyChatParticipant(chatId, currentUser.getId());

        Map<Long, UserResponse> userMap = getUsersMap(chat.getParticipantIds());
        Map<Long, Boolean> onlineMap = presenceService.getUsersOnlineStatus(chat.getParticipantIds());

        Message latestMessage = messageRepository.findFirstByChatIdOrderByTimestampDesc(chatId).orElse(null);
        return buildChatResponse(chat, latestMessage, userMap, onlineMap);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable, User currentUser) {
        findAndVerifyChatParticipant(chatId, currentUser.getId());

        Page<Message> messages = messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable);

        Set<Long> authorIds = messages.stream().map(Message::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserResponse> authorsMap = getUsersMap(authorIds);
        Map<Long, Boolean> onlineMap = presenceService.getUsersOnlineStatus(authorIds);

        return messages.map(msg -> {
            UserResponse author = authorsMap.get(msg.getAuthorId());
            boolean isOnline = onlineMap.getOrDefault(msg.getAuthorId(), false);

            UserSummaryResponse userSummary = (author != null)
                    ? new UserSummaryResponse(author.id(), author.username(), isOnline)
                    : new UserSummaryResponse(msg.getAuthorId(), "Unknown", false);

            String contentUrl = (msg.getType() == MessageType.TEXT)
                    ? msg.getContent()
                    : mediaService.getPresignedUrl(msg.getContent());

            return new MessageResponse(
                    msg.getId(),
                    contentUrl,
                    msg.getType(),
                    msg.getTimestamp(),
                    userSummary
            );
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserIsParticipant(Long chatId, Long userId) {
        findAndVerifyChatParticipant(chatId, userId);
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

    private ChatResponse toChatResponse(Chat chat, User currentUser, UserResponse partner, Map<Long, Boolean> onlineMap) {
        Set<UserSummaryResponse> participants = Set.of(
                new UserSummaryResponse(currentUser.getId(), currentUser.getUsername(), onlineMap.getOrDefault(currentUser.getId(), true)),
                new UserSummaryResponse(partner.id(), partner.username(), onlineMap.getOrDefault(partner.id(), false))
        );
        return new ChatResponse(chat.getId(), chat.getName(), chat.getType(), participants, null);
    }

    private ChatResponse buildChatResponse(Chat chat, Message latestMsg,
                                           Map<Long, UserResponse> userMap,
                                           Map<Long, Boolean> onlineMap) {
        Set<UserSummaryResponse> participants = chat.getParticipantIds().stream()
                .map(id -> {
                    UserResponse u = userMap.get(id);
                    boolean isOnline = onlineMap.getOrDefault(id, false);

                    return u != null
                            ? new UserSummaryResponse(u.id(), u.username(), isOnline)
                            : new UserSummaryResponse(id, "Unknown", false);
                })
                .collect(Collectors.toSet());

        MessageResponse msgResponse = null;
        if (latestMsg != null) {
            UserResponse author = userMap.get(latestMsg.getAuthorId());
            boolean authorOnline = onlineMap.getOrDefault(latestMsg.getAuthorId(), false);

            UserSummaryResponse authorSummary = (author != null)
                    ? new UserSummaryResponse(author.id(), author.username(), authorOnline)
                    : new UserSummaryResponse(latestMsg.getAuthorId(), "Unknown", false);

            String contentUrl = (latestMsg.getType() == MessageType.TEXT)
                    ? latestMsg.getContent()
                    : mediaService.getPresignedUrl(latestMsg.getContent());

            msgResponse = new MessageResponse(
                    latestMsg.getId(),
                    contentUrl,
                    latestMsg.getType(),
                    latestMsg.getTimestamp(),
                    authorSummary
            );
        }

        return new ChatResponse(chat.getId(), chat.getName(), chat.getType(), participants, msgResponse);
    }
}
