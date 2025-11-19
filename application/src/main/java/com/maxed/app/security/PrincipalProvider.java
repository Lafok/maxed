package com.maxed.app.security;

import com.maxed.userservice.api.User; // Используем API-версию User
import com.maxed.userservice.impl.UserRepository; // Добавим зависимость
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor // Добавим RequiredArgsConstructor для внедрения UserRepository
public class PrincipalProvider {

    private final UserRepository userRepository; // Внедряем UserRepository

    public User getAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated.");
        }

        Object principal = authentication.getPrincipal();
        com.maxed.userservice.impl.User implUser = null;

        if (principal instanceof com.maxed.userservice.impl.User) {
            implUser = (com.maxed.userservice.impl.User) principal;
        } else if (principal instanceof UserDetails) {
            // Если это UserDetails, но не наш impl.User, то загрузим его по username
            String username = ((UserDetails) principal).getUsername();
            implUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
        } else {
            throw new IllegalStateException("Authenticated user principal is not an instance of UserDetails or com.maxed.userservice.impl.User, but: " + principal.getClass().getName());
        }

        // Преобразуем impl.User в api.User
        return User.builder()
                .id(implUser.getId())
                .username(implUser.getUsername())
                .email(implUser.getEmail())
                .role(implUser.getRole())
                .build();
    }
}
