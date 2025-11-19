package com.maxed.userservice.impl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// TODO
// Instead of UserRepository create UserApiClient which will create REST-request to user-service using
// his API (GET /api/v1/users/{id})
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
