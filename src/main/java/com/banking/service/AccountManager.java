package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import main.java.com.banking.model.AccountType;
import main.java.com.banking.exception.*;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Account Management Engine - With Role-Based Access Control
 * Handles all account operations with proper authorization
 */
public class AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private Map<String, Account> accountMap;
    private static AccountManager instance;
    private AuthenticationService authService;

    private AccountManager() {
        accountMap = new HashMap<>();
        authService = AuthenticationService.getInstance();
        loadAccountsFromDatabase();
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }

    /**
     * Create a new account - ADMIN ONLY
     */
    public Account createAccount(String accountId, String holderName, double initialBalance,
                                 AccountType accountType, String email, String phone)
            throws Exception {

        // ✅ Authorization Check - Only Admin can create accounts
        authService.requireAdmin();

        logger.info("Account creation initiated by: {} for account: {}",
                authService.getCurrentUser().getUsername(), accountId);

        // Sanitize inputs
        accountId = ValidationUtil.sanitizeAccountId(accountId);
        holderName = ValidationUtil.sanitizeName(holderName);
        email = ValidationUtil.sanitizeEmail(email);
        phone = ValidationUtil.sanitizePhone(phone);

        // Validate Account ID
        if (!ValidationUtil.isValidAccountId(accountId)) {
            logger.warn("Invalid account ID format: {}", accountId);
            throw new InvalidAmountException(ValidationUtil.getAccountIdError() + " | You entered: '" + accountId + "'");
        }

        // Validate Holder Name
        if (!ValidationUtil.isValidName(holderName)) {
            logger.warn("Invalid holder name: {}", holderName);
            throw new InvalidAmountException(ValidationUtil.getNameError() + " | You entered: '" + holderName + "'");
        }

        // Validate Initial Balance
        if (!ValidationUtil.isValidInitialBalance(initialBalance)) {
            logger.warn("Invalid initial balance: {}", initialBalance);
            throw new InvalidAmountException(ValidationUtil.getInitialBalanceError());
        }

        // Validate Email
        if (!ValidationUtil.isValidEmail(email)) {
            logger.warn("Invalid email: {}", email);
            throw new InvalidAmountException(ValidationUtil.getEmailError() + " | You entered: '" + email + "'");
        }

        // Validate Phone
        if (!ValidationUtil.isValidPhone(phone)) {
            logger.warn("Invalid phone: {}", phone);
            throw new InvalidAmountException(ValidationUtil.getPhoneError() + " | You entered: '" + phone + "'");
        }

        // Check if account already exists
        if (accountMap.containsKey(accountId)) {
            logger.warn("Duplicate account creation attempt: {}", accountId);
            throw new DuplicateAccountException("Account with ID " + accountId + " already exists");
        }

        // Create account object
        Account account = new Account(accountId, holderName, initialBalance, accountType, email, phone);

        // Save to database
        saveAccountToDatabase(account);

        // Add to in-memory map
        accountMap.put(accountId, account);

        logger.info("Account created successfully: {} by user: {}",
                accountId, authService.getCurrentUser().getUsername());
        auditLogger.info("ACCOUNT_CREATED | Account: {} | Holder: {} | Balance: {} | Created by: {} ({})",
                accountId, holderName, initialBalance,
                authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        System.out.println("✓ Account created successfully: " + accountId);
        return account;
    }

    /**
     * Get account by ID - With Access Control
     */
    public Account getAccount(String accountId) throws Exception {
        // ✅ Authorization Check
        authService.requireAuthentication();

        // Check if user can access this account
        if (!authService.canAccessAccount(accountId)) {
            logger.warn("Unauthorized account access attempt: {} by user: {}",
                    accountId, authService.getCurrentUser().getUsername());
            auditLogger.warn("ACCESS_DENIED | User: {} | Attempted to access: {} | Own account: {}",
                    authService.getCurrentUser().getUsername(), accountId,
                    authService.getCurrentUser().getLinkedAccountId());
            throw new Exception("Access denied. You can only view your own account.");
        }

        if (!accountMap.containsKey(accountId)) {
            logger.warn("Account not found: {}", accountId);
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        logger.debug("Account accessed: {} by user: {}", accountId, authService.getCurrentUser().getUsername());
        return accountMap.get(accountId);
    }

    /**
     * Get account by ID - INTERNAL USE (for system services)
     * No authentication required - for internal service use only
     */
    protected Account getAccountInternal(String accountId) throws AccountNotFoundException {
        if (!accountMap.containsKey(accountId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return accountMap.get(accountId);
    }

    /**
     * Get all accounts - ADMIN/EMPLOYEE ONLY
     */
    public List<Account> getAllAccounts() throws Exception {
        // ✅ Authorization Check - Only Admin and Employee can view all accounts
        authService.requireEmployeeOrAdmin();

        logger.info("All accounts viewed by: {} ({})",
                authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        return new ArrayList<>(accountMap.values());
    }

    /**
     * Get all accounts - INTERNAL USE (for system services like monitoring)
     * No authentication required - for background services
     */
    protected List<Account> getAllAccountsInternal() {
        return new ArrayList<>(accountMap.values());
    }

    /**
     * Update account balance - Internal use only
     */
    public void updateBalance(String accountId, double newBalance)
            throws Exception {
        // ✅ Authorization Check
        authService.requireAuthentication();

        Account account = accountMap.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        account.setBalance(newBalance);
        updateAccountInDatabase(account);

        logger.debug("Balance updated for account: {} | New balance: {}", accountId, newBalance);
    }

    /**
     * Delete account - ADMIN ONLY
     */
    public void deleteAccount(String accountId) throws Exception {
        // ✅ Authorization Check - Only Admin can delete accounts
        authService.requireAdmin();

        if (!accountMap.containsKey(accountId)) {
            logger.warn("Delete attempt on non-existent account: {}", accountId);
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        // Remove from database
        deleteAccountFromDatabase(accountId);

        // Remove from map
        accountMap.remove(accountId);

        logger.info("Account deleted: {} by user: {}",
                accountId, authService.getCurrentUser().getUsername());
        auditLogger.info("ACCOUNT_DELETED | Account: {} | Deleted by: {} ({})",
                accountId, authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        System.out.println("✓ Account deleted successfully: " + accountId);
    }

    /**
     * Check if account exists
     */
    public boolean accountExists(String accountId) {
        return accountMap.containsKey(accountId);
    }

    /**
     * Get total number of accounts - ADMIN/EMPLOYEE ONLY
     */
    public int getTotalAccounts() throws Exception {
        authService.requireEmployeeOrAdmin();
        return accountMap.size();
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Save account to database
     */
    private void saveAccountToDatabase(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_id, holder_name, balance, account_type, email, phone, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountId());
            stmt.setString(2, account.getHolderName());
            stmt.setDouble(3, account.getBalance());
            stmt.setString(4, account.getAccountType().name());
            stmt.setString(5, account.getEmail());
            stmt.setString(6, account.getPhone());

            // Track who created this account
            if (authService.isLoggedIn()) {
                stmt.setInt(7, authService.getCurrentUser().getUserId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();
            logger.debug("Account saved to database: {}", account.getAccountId());
        }
    }

    /**
     * Load all accounts from database
     */
    private void loadAccountsFromDatabase() {
        String sql = "SELECT * FROM accounts WHERE status = 'ACTIVE'";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String accountId = rs.getString("account_id");
                String holderName = rs.getString("holder_name");
                double balance = rs.getDouble("balance");
                AccountType type = AccountType.valueOf(rs.getString("account_type"));
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                Account account = new Account(accountId, holderName, balance, type, email, phone);
                accountMap.put(accountId, account);
            }

            logger.info("Loaded {} accounts from database", accountMap.size());
            System.out.println("✓ Loaded " + accountMap.size() + " accounts from database");

        } catch (SQLException e) {
            logger.error("Error loading accounts from database", e);
            System.err.println("Error loading accounts from database: " + e.getMessage());
        }
    }

    /**
     * Update account in database
     */
    private void updateAccountInDatabase(Account account) throws SQLException {
        String sql = "UPDATE accounts SET holder_name = ?, balance = ?, email = ?, phone = ? " +
                "WHERE account_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getHolderName());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getEmail());
            stmt.setString(4, account.getPhone());
            stmt.setString(5, account.getAccountId());

            stmt.executeUpdate();
            logger.debug("Account updated in database: {}", account.getAccountId());
        }
    }

    /**
     * Delete account from database (soft delete)
     */
    private void deleteAccountFromDatabase(String accountId) throws SQLException {
        String sql = "UPDATE accounts SET status = 'CLOSED' WHERE account_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.executeUpdate();
            logger.debug("Account marked as closed in database: {}", accountId);
        }
    }

    /**
     * Display all accounts - With Access Control
     */
    public void displayAllAccounts() throws Exception {
        // ✅ Authorization Check
        authService.requireEmployeeOrAdmin();

        System.out.println("\n========== ALL ACCOUNTS ==========");
        if (accountMap.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (Account account : accountMap.values()) {
                System.out.println(account);
            }
        }
        System.out.println("==================================\n");

        logger.info("Account list displayed by: {}", authService.getCurrentUser().getUsername());
    }

    /**
     * Display customer's own account only
     */
    public void displayMyAccount() throws Exception {
        authService.requireAuthentication();

        if (!authService.isCustomer()) {
            throw new Exception("This function is for customers only");
        }

        String accountId = authService.getCurrentUser().getLinkedAccountId();
        if (accountId == null) {
            System.out.println("No account linked to your user profile.");
            return;
        }

        Account account = getAccount(accountId);
        System.out.println("\n========== MY ACCOUNT ==========");
        System.out.println(account);
        System.out.println("================================\n");
    }
}