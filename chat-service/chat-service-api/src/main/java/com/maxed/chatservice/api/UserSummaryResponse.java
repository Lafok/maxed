package com.maxed.chatservice.api;

public record UserSummaryResponse(Long id,
                                  String username,
                                  boolean isOnline) {
}
