package com.maxed.userservice.api;

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
}
