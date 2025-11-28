package com.maxed.chatservice.api;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id, 
    String content, 
    MessageType type,
    LocalDateTime timestamp, 
    UserSummaryResponse author
) {}
