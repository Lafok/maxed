package com.maxed.chatservice.api;

import com.maxed.userservice.api.User; // Используем API-версию User
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatResponse createDirectChat(CreateDirectChatRequest request, User currentUser);
    List<ChatResponse> getChatsForCurrentUser(User currentUser);
    ChatResponse getChatById(Long chatId, User currentUser);
    MessageResponse sendMessage(Long chatId, SendMessageRequest request, User currentUser);
    Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable, User currentUser);
}
