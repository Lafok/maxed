package com.maxed.userservice.api;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);

    Optional<UserResponse> getUserById(Long id);

    List<UserResponse> getAllUsers();

    Optional<UserResponse> updateUser(Long id, UserRequest userRequest);

    boolean deleteUser(Long id);
}
