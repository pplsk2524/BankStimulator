package main.java.com.banking.service;

import main.java.com.banking.model.User;
import main.java.com.banking.model.UserRole;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.util.SecurityUtil;
import main.java.com.banking.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * User Manager Service
 * Handles user-related operations like creating users, changing passwords, etc.
 */
public class UserManager {

    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static UserManager instance;
    private AuthenticationService authService;
    private AccountManager accountManager;

    private UserManager() {
        this.authService = AuthenticationService.getInstance();
        this.accountManager = AccountManager.getInstance();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Change password for current user
     */
    public void changePassword(String currentPassword, String newPassword) throws Exception {
        authService.requireAuthentication();

        User currentUser = authService.getCurrentUser();

        logger.info("Password change requested by user: {}", currentUser.getUsername());

        // Verify current password
        if (!SecurityUtil.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
            logger.warn("Password change failed: Invalid current password for user: {}",
                    currentUser.getUsername());
            auditLogger.warn("PASSWORD_CHANGE_FAILED | User: {} | Reason: INVALID_CURRENT_PASSWORD",
                    currentUser.getUsername());
            throw new Exception("Current password is incorrect");
        }

        // Validate new password strength
        if (!SecurityUtil.isStrongPassword(newPassword)) {
            logger.warn("Password change failed: Weak password for user: {}", currentUser.getUsername());
            throw new Exception(SecurityUtil.getPasswordStrengthMessage());
        }

        // Check if new password is same as current
        if (SecurityUtil.verifyPassword(newPassword, currentUser.getPasswordHash())) {
            logger.warn("Password change failed: New password same as old for user: {}",
                    currentUser.getUsername());
            throw new Exception("New password must be different from current password");
        }

        // Hash new password
        String newPasswordHash = SecurityUtil.hashPassword(newPassword);

        // Update in database
        updatePasswordInDatabase(currentUser.getUserId(), newPasswordHash);

        // Update current user object
        currentUser.setPasswordHash(newPasswordHash);

        logger.info("Password changed successfully for user: {}", currentUser.getUsername());
        auditLogger.info("PASSWORD_CHANGED | User: {} | Time: {}",
                currentUser.getUsername(), java.time.LocalDateTime.now());

        System.out.println("\n✓ Password changed successfully!");
        System.out.println("Please use your new password for future logins.");
    }

    /**
     * Admin can reset user password
     */
    public String resetUserPassword(String username) throws Exception {
        authService.requireAdmin();

        logger.info("Password reset requested by admin: {} for user: {}",
                authService.getCurrentUser().getUsername(), username);

        // Load user
        User user = loadUserByUsername(username);
        if (user == null) {
            throw new Exception("User not found: " + username);
        }

        // Generate temporary password
        String tempPassword = SecurityUtil.generateTemporaryPassword();
        String passwordHash = SecurityUtil.hashPassword(tempPassword);

        // Update in database
        updatePasswordInDatabase(user.getUserId(), passwordHash);

        // Reset failed login attempts
        resetFailedAttempts(user.getUserId());

        // Unlock account if locked
        unlockUser(user.getUserId());

        logger.info("Password reset by admin for user: {}", username);
        auditLogger.info("PASSWORD_RESET | Admin: {} | Target User: {} | Temp Password Generated",
                authService.getCurrentUser().getUsername(), username);

        System.out.println("\n✓ Password reset successfully!");
        System.out.println("Temporary password: " + tempPassword);
        System.out.println("⚠️  User should change this password after first login.");

        return tempPassword;
    }

    /**
     * Create customer user linked to account (ADMIN ONLY)
     */
    public User createCustomerUser(String accountId, String username, String tempPassword)
            throws Exception {
        authService.requireAdmin();

        logger.info("Customer user creation initiated by: {} for account: {}",
                authService.getCurrentUser().getUsername(), accountId);

        // Validate account exists
        if (!accountManager.accountExists(accountId)) {
            throw new Exception("Account not found: " + accountId);
        }

        // Get account details
        var account = accountManager.getAccountInternal(accountId);

        // Validate username
        if (username == null || username.trim().length() < 3) {
            throw new Exception("Username must be at least 3 characters");
        }

        // Check if username already exists
        if (loadUserByUsername(username) != null) {
            throw new Exception("Username already exists: " + username);
        }

        // Validate password
        if (!SecurityUtil.isStrongPassword(tempPassword)) {
            throw new Exception(SecurityUtil.getPasswordStrengthMessage());
        }

        // Hash password
        String passwordHash = SecurityUtil.hashPassword(tempPassword);

        // Create user
        User user = new User(
                username,
                passwordHash,
                UserRole.CUSTOMER,
                account.getHolderName(),
                account.getEmail(),
                account.getPhone()
        );
        user.setLinkedAccountId(accountId);

        // Save to database
        saveUserToDatabase(user);

        logger.info("Customer user created: {} linked to account: {}", username, accountId);
        auditLogger.info("USER_CREATED | Admin: {} | Username: {} | Role: CUSTOMER | Account: {}",
                authService.getCurrentUser().getUsername(), username, accountId);

        System.out.println("\n✓ Customer user created successfully!");
        System.out.println("Username: " + username);
        System.out.println("Temporary Password: " + tempPassword);
        System.out.println("Linked Account: " + accountId);
        System.out.println("⚠️  User should change password after first login.");

        return user;
    }

    /**
     * Unlock user account (ADMIN ONLY)
     */
    public void unlockUserAccount(String username) throws Exception {
        authService.requireAdmin();

        User user = loadUserByUsername(username);
        if (user == null) {
            throw new Exception("User not found: " + username);
        }

        unlockUser(user.getUserId());
        resetFailedAttempts(user.getUserId());

        logger.info("User account unlocked by admin: {}", username);
        auditLogger.info("ACCOUNT_UNLOCKED | Admin: {} | Target User: {}",
                authService.getCurrentUser().getUsername(), username);

        System.out.println("✓ User account unlocked: " + username);
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Update password in database
     */
    private void updatePasswordInDatabase(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            logger.debug("Password updated in database for user ID: {}", userId);
        }
    }

    /**
     * Save new user to database
     */
    private void saveUserToDatabase(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email, phone, " +
                "linked_account_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getLinkedAccountId());
            stmt.setBoolean(8, true);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setUserId(rs.getInt(1));
            }

            logger.debug("User saved to database: {}", user.getUsername());
        }
    }

    /**
     * Load user by username
     */
    private User loadUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        UserRole.valueOf(rs.getString("role")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("linked_account_id"),
                        rs.getBoolean("is_active"),
                        rs.getInt("failed_login_attempts"),
                        rs.getTimestamp("last_login_time") != null ?
                                rs.getTimestamp("last_login_time").toLocalDateTime() : null,
                        rs.getTimestamp("created_date").toLocalDateTime()
                );
            }
        }

        return null;
    }

    /**
     * Reset failed login attempts
     */
    private void resetFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = 0 WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Unlock user
     */
    private void unlockUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = TRUE WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}