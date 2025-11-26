package com.maxed.chatservice.api;

import java.util.Set;

public record ChatResponse(Long id,
                           String name,
                           ChatType type,
                           Set<UserSummaryResponse> participants,
                           MessageResponse latestMessage) {
}
