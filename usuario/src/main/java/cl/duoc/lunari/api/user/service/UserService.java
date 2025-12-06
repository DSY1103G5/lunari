package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.dto.UpdateProfileRequest;
import cl.duoc.lunari.api.user.model.Coupon;
import cl.duoc.lunari.api.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * User service interface for authentication and profile management.
 *
 * Focused on 8 core endpoints:
 * - login, register, getprofile, updateprofile, getcoupons,
 *   redeempoints, removecoupon, addpoints
 */
public interface UserService {

    // ==================== User Management ====================

    /**
     * Create a new user with BCrypt hashed password.
     *
     * @param user User to create (ID will be generated automatically)
     * @return Created user with assigned ID
     */
    User createUser(User user);

    /**
     * Authenticate user with email/username and password.
     * Supports BCrypt verification and automatic migration from plain text passwords.
     *
     * @param identifier Email or username
     * @param password Plain text password
     * @return Authenticated user if credentials are valid
     * @throws RuntimeException if credentials are invalid or user is inactive
     */
    User authenticateUser(String identifier, String password);

    /**
     * Get user by ID.
     *
     * @param userId User ID (UUID as String)
     * @return Optional with user if exists
     */
    Optional<User> getUserById(String userId);

    /**
     * Get user by email.
     *
     * @param email User email
     * @return Optional with user if exists
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Update user profile (personal info, address, gaming, preferences).
     *
     * @param userId User ID
     * @param request Profile update request with optional fields
     * @return Updated user
     */
    User updateUserProfile(String userId, UpdateProfileRequest request);

    // ==================== Points Management ====================

    /**
     * Add points to user.
     *
     * @param userId User ID
     * @param points Points to add (can be negative to subtract)
     * @return Updated user
     */
    User addPoints(String userId, Long points);

    // ==================== Coupon Management ====================

    /**
     * Get all coupons for a user.
     *
     * @param userId User ID
     * @return List of user's coupons
     */
    List<Coupon> getClientCoupons(String userId);

    /**
     * Redeem points for a coupon using tiered conversion rates.
     * Conversion rates by level:
     * - Bronze: 0.01 (100 points = $1.00)
     * - Silver: 0.015 (100 points = $1.50)
     * - Gold: 0.02 (100 points = $2.00)
     * - Platinum: 0.025 (100 points = $2.50)
     *
     * @param userId User ID
     * @param pointsToRedeem Points to redeem (minimum 100)
     * @return Created coupon
     * @throws RuntimeException if insufficient points or less than minimum
     */
    Coupon redeemPoints(String userId, Long pointsToRedeem);

    /**
     * Remove a coupon from user.
     *
     * @param userId User ID
     * @param couponId Coupon ID to remove
     * @throws RuntimeException if coupon not found
     */
    void removeCoupon(String userId, String couponId);
}
