package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.dto.UpdateProfileRequest;
import cl.duoc.lunari.api.user.model.*;
import cl.duoc.lunari.api.user.repository.UserRepository;
import cl.duoc.lunari.api.user.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

/**
 * Implementación del servicio de usuarios con autenticación JWT y gestión de cupones.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== User Management ====================

    @Override
    public User createUser(User user) {
        // Validate unique email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Generate ID
        user.setId(UUID.randomUUID().toString());

        // Hash password with BCrypt
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Set timestamps
        String now = DateTimeUtil.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Initialize personal info
        if (user.getPersonal() == null) {
            user.setPersonal(new Personal());
        }
        if (user.getPersonal().getMemberSince() == null) {
            user.getPersonal().setMemberSince(String.valueOf(Year.now().getValue()));
        }

        // Initialize client stats with default level and zero points
        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }
        // Ensure Bronze level for new users
        user.getStats().setLevel("Bronze");
        if (user.getStats().getPoints() == null) {
            user.getStats().setPoints(0L);
        }

        // Initialize preferences
        if (user.getPreferences() == null) {
            user.setPreferences(ClientPreferences.createDefault());
        }

        // Initialize empty coupons list
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }

        // Set initial status
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getIsVerified() == null) {
            user.setIsVerified(false);
        }

        log.info("Creating new user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User authenticateUser(String identifier, String password) {
        log.info("Attempting authentication for identifier: {}", identifier);

        // Try email first (most common)
        Optional<User> userOptional = userRepository.findByEmail(identifier);

        // If not found by email, try username (if findByUsername is implemented)
        // For now, only support email authentication
        if (userOptional.isEmpty()) {
            log.warn("User not found with identifier: {}", identifier);
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOptional.get();

        // Verify user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Inactive user attempted login: {}", identifier);
            throw new RuntimeException("Account is inactive. Please contact support.");
        }

        // Password verification with BCrypt migration support
        String userPassword = user.getPassword();
        boolean passwordMatches = false;

        // Check if password is BCrypt hashed
        if (userPassword.startsWith("$2a$") || userPassword.startsWith("$2b$")) {
            // BCrypt password - use encoder
            passwordMatches = passwordEncoder.matches(password, userPassword);
        } else {
            // Plain text password (legacy) - compare directly and migrate
            if (password.equals(userPassword)) {
                passwordMatches = true;
                // Auto-upgrade to BCrypt
                log.info("Migrating plain text password to BCrypt for user: {}", identifier);
                user.setPassword(passwordEncoder.encode(password));
                user.setUpdatedAt(DateTimeUtil.now());
                userRepository.save(user);
            }
        }

        if (!passwordMatches) {
            log.warn("Invalid password for user: {}", identifier);
            throw new RuntimeException("Invalid credentials");
        }

        log.info("Successful authentication for user: {} (ID: {})", identifier, user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUserProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update personal info
        if (request.getFirstName() != null || request.getLastName() != null ||
            request.getPhone() != null || request.getBirthdate() != null ||
            request.getBio() != null || request.getAvatar() != null) {

            Personal personal = user.getPersonal() != null ? user.getPersonal() : new Personal();

            if (request.getFirstName() != null) {
                personal.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                personal.setLastName(request.getLastName());
            }
            if (request.getPhone() != null) {
                personal.setPhone(request.getPhone());
            }
            if (request.getBirthdate() != null) {
                personal.setBirthdate(request.getBirthdate());
            }
            if (request.getBio() != null) {
                personal.setBio(request.getBio());
            }
            if (request.getAvatar() != null) {
                personal.setAvatar(request.getAvatar());
            }

            user.setPersonal(personal);
        }

        // Update address
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        // Update gaming profile
        if (request.getGaming() != null) {
            user.setGaming(request.getGaming());
        }

        // Update preferences
        if (request.getPreferences() != null) {
            user.setPreferences(request.getPreferences());
        }

        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated profile for user: {}", userId);
        return userRepository.save(user);
    }

    // ==================== Points Management ====================

    @Override
    public User addPoints(String userId, Long points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().addPoints(points);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Added {} points to user {}: Total: {}", points, userId, user.getStats().getPoints());
        return userRepository.save(user);
    }

    // ==================== Coupon Management ====================

    @Override
    public List<Coupon> getClientCoupons(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return user.getCoupons() != null ? user.getCoupons() : new ArrayList<>();
    }

    @Override
    public Coupon redeemPoints(String userId, Long pointsToRedeem) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Validate sufficient points
        Long currentPoints = user.getStats().getPoints();
        if (currentPoints < pointsToRedeem) {
            throw new RuntimeException("Insufficient points. You have " + currentPoints + " points");
        }

        // Validate minimum points
        if (pointsToRedeem < 100) {
            throw new RuntimeException("Minimum 100 points required for redemption");
        }

        // Get conversion rate based on user level
        String level = user.getLevel();
        double conversionRate = getConversionRate(level);
        double couponValue = pointsToRedeem * conversionRate;

        // Generate coupon
        Coupon coupon = new Coupon();
        coupon.setId(UUID.randomUUID().toString());
        coupon.setCode(generateCouponCode(level, user.getId()));
        coupon.setDescription("Redeemed " + pointsToRedeem + " points as " + level + " member");
        coupon.setType("fixed");
        coupon.setValue(couponValue);
        coupon.setMinPurchase(0.0);
        coupon.setExpiresAt(LocalDate.now().plusDays(90).toString());
        coupon.setIsUsed(false);

        // Deduct points
        user.getStats().setPoints(currentPoints - pointsToRedeem);

        // Add coupon to user's list
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }
        user.getCoupons().add(coupon);

        user.setUpdatedAt(DateTimeUtil.now());
        userRepository.save(user);

        log.info("User {} redeemed {} points for ${} coupon ({})", userId, pointsToRedeem, couponValue, level);
        return coupon;
    }

    @Override
    public void removeCoupon(String userId, String couponId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getCoupons() == null || user.getCoupons().isEmpty()) {
            throw new RuntimeException("No coupons to remove");
        }

        boolean removed = user.getCoupons().removeIf(c -> couponId.equals(c.getId()));

        if (!removed) {
            throw new RuntimeException("Coupon not found with ID: " + couponId);
        }

        user.setUpdatedAt(DateTimeUtil.now());
        userRepository.save(user);

        log.info("Removed coupon {} from user {}", couponId, userId);
    }

    // ==================== Helper Methods ====================

    /**
     * Get conversion rate based on user level (tiered system)
     * Bronze: 0.01 (100 points = $1.00)
     * Silver: 0.015 (100 points = $1.50)
     * Gold: 0.02 (100 points = $2.00)
     * Platinum: 0.025 (100 points = $2.50)
     */
    private double getConversionRate(String level) {
        if (level == null) {
            return 0.01; // Default to Bronze rate
        }

        return switch (level.toLowerCase()) {
            case "bronze" -> 0.01;
            case "silver" -> 0.015;
            case "gold" -> 0.02;
            case "platinum" -> 0.025;
            default -> 0.01;
        };
    }

    /**
     * Generate unique coupon code
     * Format: {LEVEL-PREFIX}-{USER-ID-8-CHARS}-{RANDOM-4-DIGITS}
     * Example: GOL-A1B2C3D4-5678
     */
    private String generateCouponCode(String level, String userId) {
        String levelPrefix = level != null ?
                level.substring(0, Math.min(3, level.length())).toUpperCase() : "DEF";
        String userSuffix = userId.substring(0, Math.min(8, userId.length())).toUpperCase();
        String random = String.format("%04d", new Random().nextInt(10000));

        return levelPrefix + "-" + userSuffix + "-" + random;
    }
}
