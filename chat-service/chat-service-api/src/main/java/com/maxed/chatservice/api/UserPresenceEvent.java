package com.maxed.chatservice.api;

public record UserPresenceEvent(Long userId, boolean isOnline) {
}
