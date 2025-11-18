package main.java.com.banking.service;

import main.java.com.banking.model.User;
import main.java.com.banking.model.UserRole;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Authentication Service - With Logging
 * Handles user login, logout, and session management
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static AuthenticationService instance;
    private User currentUser;
    private LocalDateTime sessionStartTime;

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private AuthenticationService() {}

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Authenticate user
     */
    public User login(String username, String password) throws Exception {
        logger.info("Login attempt for username: {}", username);
        auditLogger.info("LOGIN_ATTEMPT | Username: {}", username);

        // Load user from database
        User user = loadUserByUsername(username);

        if (user == null) {
            logger.warn("Login failed: User not found - {}", username);
            auditLogger.warn("LOGIN_FAILED | Username: {} | Reason: USER_NOT_FOUND", username);
            throw new Exception("Invalid username or password");
        }

        // Check if account is active
        if (!user.isActive()) {
            logger.warn("Login failed: Account locked - {}", username);
            auditLogger.warn("LOGIN_FAILED | Username: {} | Reason: ACCOUNT_LOCKED", username);
            throw new Exception("Account is locked. Contact administrator.");
        }

        // Check failed login attempts
        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            logger.warn("Login failed: Max attempts exceeded - {}", username);
            lockUser(user.getUserId());
            auditLogger.warn("LOGIN_FAILED | Username: {} | Reason: MAX_ATTEMPTS_EXCEEDED | Account locked", username);
            throw new Exception("Account locked due to multiple failed attempts. Contact administrator.");
        }

        // Verify password using BCrypt
        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
            // Increment failed attempts
            incrementFailedAttempts(user.getUserId());
            user.incrementFailedAttempts();

            int attemptsLeft = MAX_LOGIN_ATTEMPTS - user.getFailedLoginAttempts();
            logger.warn("Login failed: Invalid password - {} (Attempts left: {})", username, attemptsLeft);
            auditLogger.warn("LOGIN_FAILED | Username: {} | Reason: INVALID_PASSWORD | Attempts left: {}",
                    username, attemptsLeft);

            throw new Exception("Invalid username or password. Attempts left: " + attemptsLeft);
        }

        // Successful login
        resetFailedAttempts(user.getUserId());
        updateLastLogin(user.getUserId());

        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();

        logger.info("Login successful: {} (Role: {})", username, user.getRole());
        auditLogger.info("LOGIN_SUCCESS | User ID: {} | Username: {} | Role: {} | Time: {}",
                user.getUserId(), username, user.getRole(), sessionStartTime);

        System.out.println("\n✓ Login successful!");
        System.out.println("Welcome, " + user.getFullName() + " (" + user.getRole().getDisplayName() + ")");

        return user;
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("Logout: {} (Session duration: {} minutes)",
                    currentUser.getUsername(), getSessionDuration());
            auditLogger.info("LOGOUT | User ID: {} | Username: {} | Session duration: {} minutes",
                    currentUser.getUserId(), currentUser.getUsername(), getSessionDuration());

            System.out.println("✓ Logout successful. Goodbye, " + currentUser.getFullName());
            currentUser = null;
            sessionStartTime = null;
        }
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user has admin role
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Check if current user has employee role
     */
    public boolean isEmployee() {
        return currentUser != null && currentUser.isEmployee();
    }

    /**
     * Check if current user has customer role
     */
    public boolean isCustomer() {
        return currentUser != null && currentUser.isCustomer();
    }

    /**
     * Require authentication
     */
    public void requireAuthentication() throws Exception {
        if (!isLoggedIn()) {
            logger.warn("Unauthorized access attempt");
            auditLogger.warn("UNAUTHORIZED_ACCESS | No user logged in");
            throw new Exception("You must be logged in to perform this action");
        }
    }

    /**
     * Require admin role
     */
    public void requireAdmin() throws Exception {
        requireAuthentication();
        if (!isAdmin()) {
            logger.warn("Access denied: Admin required - User: {}", currentUser.getUsername());
            auditLogger.warn("ACCESS_DENIED | User: {} | Required: ADMIN | Actual: {}",
                    currentUser.getUsername(), currentUser.getRole());
            throw new Exception("Access denied. Admin privileges required.");
        }
    }

    /**
     * Require employee or admin role
     */
    public void requireEmployeeOrAdmin() throws Exception {
        requireAuthentication();
        if (!isEmployee() && !isAdmin()) {
            logger.warn("Access denied: Employee/Admin required - User: {}", currentUser.getUsername());
            auditLogger.warn("ACCESS_DENIED | User: {} | Required: EMPLOYEE/ADMIN | Actual: {}",
                    currentUser.getUsername(), currentUser.getRole());
            throw new Exception("Access denied. Employee or Admin privileges required.");
        }
    }

    /**
     * Check if user can access account
     */
    public boolean canAccessAccount(String accountId) {
        if (currentUser == null) return false;

        // Admin and Employee can access all accounts
        if (isAdmin() || isEmployee()) return true;

        // Customer can only access their own account
        if (isCustomer()) {
            return accountId.equals(currentUser.getLinkedAccountId());
        }

        return false;
    }

    /**
     * Log access attempt for auditing
     */
    public void logAccess(String action, String details) {
        if (currentUser != null) {
            auditLogger.info("ACCESS | User: {} | Role: {} | Action: {} | Details: {}",
                    currentUser.getUsername(), currentUser.getRole(), action, details);
        }
    }

    // ==================== DATABASE OPERATIONS ====================

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
     * Increment failed login attempts
     */
    private void incrementFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.debug("Incremented failed login attempts for user ID: {}", userId);
        }
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
            logger.debug("Reset failed login attempts for user ID: {}", userId);
        }
    }

    /**
     * Update last login time
     */
    private void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login_time = NOW() WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.debug("Updated last login time for user ID: {}", userId);
        }
    }

    /**
     * Lock user account
     */
    private void lockUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.warn("User account locked - User ID: {}", userId);
            auditLogger.warn("ACCOUNT_LOCKED | User ID: {} | Reason: MAX_LOGIN_ATTEMPTS", userId);
        }
    }

    /**
     * Get session duration in minutes
     */
    public long getSessionDuration() {
        if (sessionStartTime == null) return 0;
        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }
}