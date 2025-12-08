package com.maxed.common.event;

import com.maxed.chatservice.api.MessageType;
import java.time.LocalDateTime;

public record MessageCreatedEvent(
    String id,
    String content,
    Long chatId,
    Long authorId,
    LocalDateTime timestamp,
    MessageType type
) {}
