package com.maxed.chatservice.api;

import java.time.LocalDateTime;

public record MessageResponse(Long id, String content, LocalDateTime timestamp, UserSummaryResponse author) {
}
