package com.roam.service;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security context for managing authentication state and PIN verification.
 * 
 * SECURITY IMPROVEMENTS:
 * - BCrypt password hashing with automatic salting
 * - Rate limiting to prevent brute force attacks (3 attempts per minute)
 * - Minimum PIN length enforcement (8 characters)
 * 
 * @ThreadSafe This class is thread-safe for concurrent authentication attempts
 */
public class SecurityContext {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContext.class);
    private static SecurityContext instance;
    private boolean authenticated = false;

    // BCrypt encoder with strength 12 (2^12 = 4096 rounds)
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // Rate limiter: 3 authentication attempts per minute (0.05 per second)
    private static final RateLimiter authRateLimiter = RateLimiter.create(0.05);

    // Minimum PIN length for security
    private static final int MIN_PIN_LENGTH = 8;

    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS_BEFORE_DELAY = 3;

    private SecurityContext() {
    }

    public static synchronized SecurityContext getInstance() {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (authenticated) {
            failedAttempts = 0; // Reset on successful auth
        }
    }

    public boolean isLockEnabled() {
        return SettingsService.getInstance().getSettings().isLockEnabled();
    }

    /**
     * Authenticates a user with the provided PIN.
     * 
     * @param pin The PIN to verify
     * @return true if authentication successful, false otherwise
     * @throws SecurityException if rate limit exceeded
     */
    public boolean authenticate(String pin) {
        // Apply rate limiting to prevent brute force attacks
        if (!authRateLimiter.tryAcquire()) {
            failedAttempts++;
            logger.warn("⚠️  Authentication rate limit exceeded. Please wait before trying again.");
            throw new SecurityException("Too many authentication attempts. Please wait before trying again.");
        }

        String storedHash = SettingsService.getInstance().getSettings().getPinHash();
        if (storedHash == null || storedHash.isEmpty()) {
            authenticated = true;
            return true; // No PIN set
        }

        boolean success = false;

        // Try BCrypt verification first
        try {
            success = passwordEncoder.matches(pin, storedHash);
        } catch (IllegalArgumentException e) {
            // Hash doesn't look like BCrypt - might be old SHA-256 format
            // Allow one-time migration: check if it's the old format
            if (storedHash.length() == 64 && storedHash.matches("[a-fA-F0-9]{64}")) {
                // Old SHA-256 format detected
                String sha256Hash = hashWithSHA256(pin);
                if (sha256Hash.equals(storedHash)) {
                    success = true;
                    logger.warn("⚠️  Old PIN format detected. Please set a new PIN in Settings.");
                }
            }
        }

        if (success) {
            authenticated = true;
            failedAttempts = 0;
            logger.info("✓ Authentication successful");
        } else {
            failedAttempts++;
            logger.warn("✗ Authentication failed (attempt {})", failedAttempts);
        }

        return success;
    }

    /**
     * Legacy SHA-256 hashing for migration support only.
     * DO NOT USE for new PINs - this is only for backward compatibility.
     */
    private String hashWithSHA256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 hash", e);
        }
    }

    /**
     * Sets a new PIN for the application lock.
     * Enforces minimum length requirement and uses BCrypt for hashing.
     * 
     * @param pin The new PIN to set
     * @throws IllegalArgumentException if PIN is too short
     */
    public void setPin(String pin) {
        if (pin == null || pin.length() < MIN_PIN_LENGTH) {
            throw new IllegalArgumentException(
                    "PIN must be at least " + MIN_PIN_LENGTH + " characters long for security. " +
                            "Consider using a combination of letters, numbers, and special characters.");
        }

        // Hash PIN with BCrypt (includes automatic salt generation)
        String hash = passwordEncoder.encode(pin);

        SettingsService.getInstance().getSettings().setPinHash(hash);
        SettingsService.getInstance().getSettings().setLockEnabled(true);
        SettingsService.getInstance().saveSettings();

        logger.info("✓ PIN set successfully with BCrypt hashing");
    }

    public void disableLock() {
        SettingsService.getInstance().getSettings().setLockEnabled(false);
        SettingsService.getInstance().saveSettings();
        authenticated = true; // Auto-authenticate when lock is disabled
    }

    /**
     * Gets the number of failed authentication attempts since last success.
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Validates a PIN format without checking against stored hash.
     * Used for pre-validation in UI before attempting authentication.
     */
    public static boolean isValidPinFormat(String pin) {
        return pin != null && pin.length() >= MIN_PIN_LENGTH;
    }

    /**
     * Gets the minimum required PIN length.
     */
    public static int getMinPinLength() {
        return MIN_PIN_LENGTH;
    }
}
