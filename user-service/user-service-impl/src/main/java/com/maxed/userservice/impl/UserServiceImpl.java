package com.maxed.userservice.impl;

import com.maxed.userservice.api.Role;
import com.maxed.userservice.api.UserRequest;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
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
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userRequest.username());
                    existingUser.setEmail(userRequest.email());
                    // Password should be updated separately if needed
                    return mapToUserResponse(userRepository.save(existingUser));
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
