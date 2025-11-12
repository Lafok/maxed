package com.maxed.userservice.impl;

import com.maxed.userservice.api.IAuthenticationFacade;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public com.maxed.userservice.api.User getAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof User internalUser) {
            return com.maxed.userservice.api.User.builder()
                    .id(internalUser.getId())
                    .username(internalUser.getUsername())
                    .email(internalUser.getEmail())
                    .role(internalUser.getRole())
                    .build();
        }

        throw new IllegalStateException("User not authenticated or principal is not of expected type User");
    }

    @Override
    public Object getPrincipal() {
        Authentication authentication = getAuthentication();
        return authentication != null ? authentication.getPrincipal() : null;
    }
}
