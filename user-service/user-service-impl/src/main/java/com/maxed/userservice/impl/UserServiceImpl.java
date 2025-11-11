package com.maxed.userservice.impl;

import com.maxed.userservice.api.Role;
import com.maxed.userservice.api.UserRequest;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRequest userRequest) {
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

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
