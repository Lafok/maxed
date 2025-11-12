package com.maxed.chatservice.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatResponse createDirectChat(CreateDirectChatRequest request);
    List<ChatResponse> getChatsForCurrentUser();
    ChatResponse getChatById(Long chatId);
    MessageResponse sendMessage(Long chatId, SendMessageRequest request);
    Page<MessageResponse> getMessagesForChat(Long chatId, Pageable pageable);
}
