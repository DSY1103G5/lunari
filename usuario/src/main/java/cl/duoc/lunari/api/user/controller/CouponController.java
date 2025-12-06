package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.user.dto.CouponResponse;
import cl.duoc.lunari.api.user.dto.RedeemPointsRequest;
import cl.duoc.lunari.api.user.model.Coupon;
import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Coupon Controller
 * Handles coupon management (protected endpoints)
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Coupon management endpoints")
@SecurityRequirement(name = "bearer-jwt")
@Slf4j
public class CouponController {

    private final UserService userService;

    @Autowired
    public CouponController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all user's coupons
     */
    @GetMapping
    @Operation(summary = "Get user coupons", description = "Get all coupons (used and unused) for authenticated user")
    public ResponseEntity<ApiResponse<List<Coupon>>> getCoupons(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        log.info("Get coupons request for: {}", email);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Coupon> coupons = userService.getClientCoupons(user.getId());

        log.info("Returning {} coupons for user: {}", coupons.size(), user.getId());
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * Redeem points for a coupon
     */
    @PostMapping("/redeem")
    @Operation(summary = "Redeem points for coupon",
               description = "Redeem points for a coupon using tiered conversion rates based on user level")
    public ResponseEntity<ApiResponse<CouponResponse>> redeemPoints(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RedeemPointsRequest request) {

        String email = userDetails.getUsername();
        log.info("Redeem points request for: {} (points: {})", email, request.getPointsToRedeem());

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Coupon coupon = userService.redeemPoints(user.getId(), request.getPointsToRedeem());

        CouponResponse response = CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .value(coupon.getValue())
                .minPurchase(coupon.getMinPurchase())
                .expiresAt(coupon.getExpiresAt())
                .message("Successfully redeemed " + request.getPointsToRedeem() +
                        " points! Coupon value: $" + String.format("%.2f", coupon.getValue()))
                .build();

        log.info("Points redeemed successfully for user: {} - Coupon: {}", user.getId(), coupon.getCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Remove a coupon
     */
    @DeleteMapping("/{couponId}")
    @Operation(summary = "Remove coupon", description = "Remove a coupon from user's account")
    public ResponseEntity<ApiResponse<Void>> removeCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String couponId) {

        String email = userDetails.getUsername();
        log.info("Remove coupon request for: {} (couponId: {})", email, couponId);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.removeCoupon(user.getId(), couponId);

        log.info("Coupon removed successfully: {}", couponId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
