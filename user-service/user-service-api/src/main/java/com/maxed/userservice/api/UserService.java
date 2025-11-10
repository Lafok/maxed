package com.maxed.userservice.api;

import java.util.List;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
}
