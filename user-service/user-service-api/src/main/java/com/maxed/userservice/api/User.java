package com.maxed.userservice.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    Long id;
    String username;
    String email;
    Role role;
}
