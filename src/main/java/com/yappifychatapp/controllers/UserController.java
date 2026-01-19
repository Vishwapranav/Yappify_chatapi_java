package com.yappifychatapp.controllers;

import com.yappifychatapp.dto.AuthResponse;
import com.yappifychatapp.dto.LoginRequest;
import com.yappifychatapp.dto.RegisterRequest;
import com.yappifychatapp.models.User;
import com.yappifychatapp.services.UserService;
import com.yappifychatapp.utils.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration, authentication, profile management and search")
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @PostMapping("/")
    @Operation(
            summary = "Register new user",
            description = "Register a new user and receive JWT token for authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid input")
    })
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest request) {
        // Validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User savedUser = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPic()
        );
        String token = jwtUtil.generateToken(savedUser.getId());
        return ResponseEntity.ok(new AuthResponse(savedUser, token));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user with email and password, returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or missing fields")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = userService.authenticateUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new AuthResponse(user, token));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Logout user (client should discard the token). Optional token blacklisting."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, String>> logout(
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @RequestHeader("Authorization") String authHeader) {
        // Extract token from "Bearer <token>"
        String token = authHeader.substring(7);

        // Optional: Add token to blacklist (implement if needed)
        // tokenBlacklistService.addToBlacklist(token);

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "info", "Please discard your token on the client side"
        ));
    }

    @GetMapping
    @Operation(
            summary = "Search users",
            description = "Search for users by name or email. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @SecurityRequirement(name = "User ID Header")
    public ResponseEntity<List<User>> searchUsers(
            @Parameter(description = "Search keyword for name or email", example = "john")
            @RequestParam(required = false) String search,
            @Parameter(description = "ID of the logged-in user", required = true)
            @RequestHeader("userId") String userId
    ) {
        List<User> users = userService.searchUsers(search != null ? search : "", userId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user profile",
            description = "Get user profile by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update user profile",
            description = "Update name and profile picture"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @SecurityRequirement(name = "User ID Header")
    public ResponseEntity<User> updateProfile(
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String pic = body.get("pic");

        User updatedUser = userService.updateProfile(userId, name, pic);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/password")
    @Operation(
            summary = "Change password",
            description = "Change user password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @SecurityRequirement(name = "User ID Header")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/account")
    @Operation(
            summary = "Delete account",
            description = "Permanently delete user account and all associated data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @SecurityRequirement(name = "User ID Header")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @RequestBody Map<String, String> body) {
        String password = body.get("password");

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password confirmation is required");
        }

        userService.deleteAccount(userId, password);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}