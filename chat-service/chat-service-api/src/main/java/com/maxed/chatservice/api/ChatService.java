package com.maxed.chatservice.api;

import java.util.List;

public interface ChatService {
    ChatResponse createDirectChat(CreateDirectChatRequest request);
    List<ChatResponse> getChatsForCurrentUser();
    ChatResponse getChatById(Long chatId);
}
