package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.user.dto.ProfileResponse;
import cl.duoc.lunari.api.user.dto.UpdateProfileRequest;
import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * User Profile Controller
 * Handles user profile operations (protected endpoints)
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearer-jwt")
@Slf4j
public class UserProfileController {

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get authenticated user's profile
     */
    @GetMapping
    @Operation(summary = "Get user profile", description = "Get authenticated user's complete profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername(); // Email is used as username
        log.info("Get profile request for: {}", email);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponse response = ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .personal(user.getPersonal())
                .address(user.getAddress())
                .gaming(user.getGaming())
                .preferences(user.getPreferences())
                .stats(user.getStats())
                .coupons(user.getCoupons())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update authenticated user's profile
     */
    @PutMapping
    @Operation(summary = "Update user profile",
               description = "Update user's personal info, address, gaming profile, and preferences")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = userDetails.getUsername();
        log.info("Update profile request for: {}", email);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User updatedUser = userService.updateUserProfile(user.getId(), request);

        ProfileResponse response = ProfileResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .personal(updatedUser.getPersonal())
                .address(updatedUser.getAddress())
                .gaming(updatedUser.getGaming())
                .preferences(updatedUser.getPreferences())
                .stats(updatedUser.getStats())
                .coupons(updatedUser.getCoupons())
                .isActive(updatedUser.getIsActive())
                .isVerified(updatedUser.getIsVerified())
                .createdAt(updatedUser.getCreatedAt() != null ? updatedUser.getCreatedAt().toString() : null)
                .build();

        log.info("Profile updated successfully for user: {}", updatedUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
