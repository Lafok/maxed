package com.maxed.userservice.api;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role
) {}
