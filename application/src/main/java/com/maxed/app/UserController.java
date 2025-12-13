package com.maxed.app;

import com.maxed.app.security.PrincipalMapper;
import com.maxed.userservice.api.User;
import com.maxed.userservice.api.UserRequest;
import com.maxed.userservice.api.UserResponse;
import com.maxed.userservice.api.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Tag(name = "User Controller", description = "Controller for managing user profiles")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PrincipalMapper principalMapper;


    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieves the details of a specific user by their ID.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users.")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name", description = "Searches for users whose name starts with the provided query, excluding the current user.")
    public ResponseEntity<List<UserResponse>> searchUsersByName(
            @RequestParam String name,
            Principal principal
    ) {
        User currentUser = principalMapper.toApiUser(principal);

        List<UserResponse> users = userService.searchUsersByName(name, currentUser.getId());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates the details of an existing user by their ID.")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        return userService.updateUser(id, userRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user account by their ID.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Upload or update user avatar",
            description = "Uploads an avatar for the currently authenticated user. The file should be an image."
    )
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) {
        User currentUser = principalMapper.toApiUser(principal);

        UserResponse response = userService.updateUserAvatar(currentUser.getId(), file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the details of the currently authenticated user.")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        User currentUser = principalMapper.toApiUser(principal);

        return userService.getUserById(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
