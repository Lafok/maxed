package com.maxed.app.security;

import com.maxed.userservice.api.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class PrincipalMapper {

    public User toApiUser(Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken token)) {
            throw new IllegalStateException("Invalid authentication principal type: " + principal.getClass().getName());
        }

        var userDetails = (com.maxed.userservice.impl.User) token.getPrincipal();

        return User.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .build();
    }
}
