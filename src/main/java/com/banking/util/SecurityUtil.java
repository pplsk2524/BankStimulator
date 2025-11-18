package main.java.com.banking.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Utility
 * Handles password hashing and verification using BCrypt
 */
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    // BCrypt work factor (higher = more secure but slower)
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Hash a password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        try {
            String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
            logger.debug("Password hashed successfully");
            return hashed;
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            if (matches) {
                logger.debug("Password verification successful");
            } else {
                logger.warn("Password verification failed");
                auditLogger.warn("Failed password verification attempt");
            }
            return matches;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Validate password strength
     * Rules:
     * - At least 8 characters
     * - Contains uppercase letter
     * - Contains lowercase letter
     * - Contains digit
     * - Contains special character (optional but recommended)
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage() {
        return "Password must be at least 8 characters with uppercase, lowercase, and numbers";
    }

    /**
     * Generate random temporary password
     */
    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
        StringBuilder password = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        logger.info("Temporary password generated");
        return password.toString();
    }
}