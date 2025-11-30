package com.maxed.chatservice.api;

public record SendMessageRequest(
    String content, 
    MessageType type
) {}
