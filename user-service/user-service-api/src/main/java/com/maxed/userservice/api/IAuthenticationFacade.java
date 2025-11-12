package com.maxed.userservice.api;

import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    Authentication getAuthentication();

    User getAuthenticatedUser();
    
    Object getPrincipal();
}
