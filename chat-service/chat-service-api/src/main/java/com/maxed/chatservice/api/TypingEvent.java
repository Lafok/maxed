package com.maxed.chatservice.api;

public record TypingEvent(
    Long chatId, 
    String username, 
    boolean isTyping
) {}
