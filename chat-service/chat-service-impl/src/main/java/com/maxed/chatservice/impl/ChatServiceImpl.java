package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.*;
import com.maxed.userservice.impl.User;
import com.maxed.userservice.impl.UserRepository;
import lombok.AllArgsConstructor;
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

    @Override
    @Transactional
    public ChatResponse createDirectChat(CreateDirectChatRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatRepository.findChatsByParticipantId(currentUser.getId()).stream()
                .map(this::toChatResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long chatId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (chat.getParticipants().stream().noneMatch(user -> user.getId().equals(currentUser.getId()))) {
            throw new RuntimeException("User not part of this chat");
        }
        return toChatResponse(chat);
    }

    private ChatResponse toChatResponse(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                chat.getParticipants().stream()
                        .map(user -> new UserSummaryResponse(user.getId(), user.getUsername()))
                        .collect(Collectors.toSet())
        );
    }
}
