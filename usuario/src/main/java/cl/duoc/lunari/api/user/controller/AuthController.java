package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.user.dto.LoginRequest;
import cl.duoc.lunari.api.user.dto.LoginResponse;
import cl.duoc.lunari.api.user.dto.ProfileResponse;
import cl.duoc.lunari.api.user.dto.RegisterRequest;
import cl.duoc.lunari.api.user.model.Personal;
import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.security.JwtUtil;
import cl.duoc.lunari.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration and login (public endpoints)
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with BCrypt password hashing")
    public ResponseEntity<ApiResponse<ProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());

        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Will be hashed in service

        // Set personal info if provided
        if (request.getFirstName() != null || request.getLastName() != null || request.getPhone() != null) {
            Personal personal = new Personal();
            personal.setFirstName(request.getFirstName());
            personal.setLastName(request.getLastName());
            personal.setPhone(request.getPhone());
            user.setPersonal(personal);
        }

        // Create user
        User createdUser = userService.createUser(user);

        // Map to response
        ProfileResponse response = ProfileResponse.builder()
                .id(createdUser.getId())
                .username(createdUser.getUsername())
                .email(createdUser.getEmail())
                .personal(createdUser.getPersonal())
                .address(createdUser.getAddress())
                .gaming(createdUser.getGaming())
                .preferences(createdUser.getPreferences())
                .stats(createdUser.getStats())
                .coupons(createdUser.getCoupons())
                .isActive(createdUser.getIsActive())
                .isVerified(createdUser.getIsVerified())
                .createdAt(createdUser.getCreatedAt())
                .build();

        log.info("User registered successfully: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Login user and generate JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getIdentifier());

        // Authenticate user
        User user = userService.authenticateUser(request.getIdentifier(), request.getPassword());

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Build response
        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .token(token)
                .level(user.getLevel())
                .points(user.getPoints())
                .message("Welcome back, " + user.getFullName() + "!")
                .build();

        log.info("User logged in successfully: {}", user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
