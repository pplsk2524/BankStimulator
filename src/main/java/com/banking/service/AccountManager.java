package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import main.java.com.banking.model.AccountType;
import main.java.com.banking.exception.*;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.util.ValidationUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Account Management Engine
 * Handles all account operations (CRUD)
 */
public class AccountManager {

    // In-memory storage using HashMap
    private Map<String, Account> accountMap;

    // Singleton instance
    private static AccountManager instance;

    // Private constructor
    private AccountManager() {
        accountMap = new HashMap<>();
        loadAccountsFromDatabase();
    }

    // Get singleton instance
    public static AccountManager getInstance() {
        if (instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }

    /**
     * Create a new account with proper validation
     */
    public Account createAccount(String accountId, String holderName, double initialBalance,
                                 AccountType accountType, String email, String phone)
            throws Exception {

        // Sanitize inputs
        accountId = ValidationUtil.sanitizeAccountId(accountId);
        holderName = ValidationUtil.sanitizeName(holderName);
        email = ValidationUtil.sanitizeEmail(email);
        phone = ValidationUtil.sanitizePhone(phone);

        // Validate Account ID
        if (!ValidationUtil.isValidAccountId(accountId)) {
            throw new InvalidAmountException(ValidationUtil.getAccountIdError());
        }

        // Validate Holder Name
        if (!ValidationUtil.isValidName(holderName)) {
            throw new InvalidAmountException(ValidationUtil.getNameError());
        }

        // Validate Initial Balance
        if (!ValidationUtil.isValidInitialBalance(initialBalance)) {
            throw new InvalidAmountException(ValidationUtil.getInitialBalanceError());
        }

        // Validate Email
        if (!ValidationUtil.isValidEmail(email)) {
            throw new InvalidAmountException(ValidationUtil.getEmailError());
        }

        // Validate Phone
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new InvalidAmountException(ValidationUtil.getPhoneError());
        }

        // Check if account already exists
        if (accountMap.containsKey(accountId)) {
            throw new DuplicateAccountException("Account with ID " + accountId + " already exists");
        }

        // Create account object
        Account account = new Account(accountId, holderName, initialBalance, accountType, email, phone);

        // Save to database
        saveAccountToDatabase(account);

        // Add to in-memory map
        accountMap.put(accountId, account);

        System.out.println("✓ Account created successfully: " + accountId);
        return account;
    }

    /**
     * Get account by ID
     */
    public Account getAccount(String accountId) throws AccountNotFoundException {
        if (!accountMap.containsKey(accountId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return accountMap.get(accountId);
    }

    /**
     * Get all accounts
     */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accountMap.values());
    }

    /**
     * Update account balance
     */
    public void updateBalance(String accountId, double newBalance)
            throws AccountNotFoundException, SQLException {
        Account account = getAccount(accountId);
        account.setBalance(newBalance);

        // Update in database
        updateAccountInDatabase(account);
    }

    /**
     * Delete account
     */
    public void deleteAccount(String accountId) throws AccountNotFoundException, SQLException {
        if (!accountMap.containsKey(accountId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        // Remove from database
        deleteAccountFromDatabase(accountId);

        // Remove from map
        accountMap.remove(accountId);

        System.out.println("✓ Account deleted successfully: " + accountId);
    }

    /**
     * Check if account exists
     */
    public boolean accountExists(String accountId) {
        return accountMap.containsKey(accountId);
    }

    /**
     * Get total number of accounts
     */
    public int getTotalAccounts() {
        return accountMap.size();
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Save account to database
     */
    private void saveAccountToDatabase(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_id, holder_name, balance, account_type, email, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountId());
            stmt.setString(2, account.getHolderName());
            stmt.setDouble(3, account.getBalance());
            stmt.setString(4, account.getAccountType().name());
            stmt.setString(5, account.getEmail());
            stmt.setString(6, account.getPhone());

            stmt.executeUpdate();
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

            System.out.println("✓ Loaded " + accountMap.size() + " accounts from database");

        } catch (SQLException e) {
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
        }
    }

    /**
     * Delete account from database
     */
    private void deleteAccountFromDatabase(String accountId) throws SQLException {
        String sql = "UPDATE accounts SET status = 'CLOSED' WHERE account_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.executeUpdate();
        }
    }

    /**
     * Display all accounts
     */
    public void displayAllAccounts() {
        System.out.println("\n========== ALL ACCOUNTS ==========");
        if (accountMap.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (Account account : accountMap.values()) {
                System.out.println(account);
            }
        }
        System.out.println("==================================\n");
    }
}