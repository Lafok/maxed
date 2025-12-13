package com.maxed.userservice.api;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);
    Optional<UserResponse> getUserById(Long id);
    List<UserResponse> getUsersByIds(Set<Long> ids);
    List<UserResponse> getAllUsers();
    Optional<UserResponse> updateUser(Long id, UserRequest userRequest);
    boolean deleteUser(Long id);
    List<UserResponse> searchUsersByName(String name, Long currentUserId);
    UserResponse updateUserAvatar(Long userId, MultipartFile avatarFile);
}
