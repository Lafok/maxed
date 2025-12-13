package com.maxed.userservice.impl;

import com.maxed.mediaservice.api.MediaService;
import com.maxed.userservice.api.Role;
import com.maxed.userservice.api.UserRequest;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MediaService mediaService;


    @Override
    @Transactional
    public UserResponse registerUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.username())) {
            throw new UsernameAlreadyExistsException("Username " + userRequest.username() + " is already taken");
        }
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new EmailAlreadyExistsException("Email " + userRequest.email() + " is already registered");
        }

        User user = User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .email(userRequest.email())
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByIds(Set<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UserResponse> updateUser(Long id, UserRequest userRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userRequest.username());
                    existingUser.setEmail(userRequest.email());
                    if (userRequest.password() != null && !userRequest.password().isBlank()) {
                        existingUser.setPassword(passwordEncoder.encode(userRequest.password()));
                    }
                    return mapToUserResponse(userRepository.save(existingUser));
                });
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByName(String name, Long currentUserId) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }

        return userRepository.findByUsernameStartingWithIgnoreCase(name).stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .limit(5) // Limit to 5 results
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUserAvatar(Long userId, MultipartFile avatarFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        String avatarKey = mediaService.uploadAvatar(avatarFile);
        user.setAvatarUrl(avatarKey);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        String avatarUrl = mediaService.getPresignedUrl(user.getAvatarUrl());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), avatarUrl);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
