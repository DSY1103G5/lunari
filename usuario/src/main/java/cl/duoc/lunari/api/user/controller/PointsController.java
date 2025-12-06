package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.user.dto.AddPointsRequest;
import cl.duoc.lunari.api.user.model.ClientStats;
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
 * Points Controller
 * Handles points management (protected endpoints)
 */
@RestController
@RequestMapping("/api/v1/points")
@Tag(name = "Points", description = "Points management endpoints")
@SecurityRequirement(name = "bearer-jwt")
@Slf4j
public class PointsController {

    private final UserService userService;

    @Autowired
    public PointsController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Add points to user
     */
    @PostMapping
    @Operation(summary = "Add points to user", description = "Add loyalty points to authenticated user's account")
    public ResponseEntity<ApiResponse<ClientStats>> addPoints(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddPointsRequest request) {

        String email = userDetails.getUsername();
        log.info("Add points request for: {} (points: {}, reason: {})",
                email, request.getPoints(), request.getReason());

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User updatedUser = userService.addPoints(user.getId(), request.getPoints());

        log.info("Points added successfully for user: {} - New total: {}",
                updatedUser.getId(), updatedUser.getStats().getPoints());

        return ResponseEntity.ok(ApiResponse.success(updatedUser.getStats()));
    }
}
