package main.java.com.banking.service;

import main.java.com.banking.model.*;
import main.java.com.banking.exception.*;
import main.java.com.banking.util.ConfigManager;
import main.java.com.banking.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Processing System - With Role-Based Access Control
 * Handles deposits, withdrawals, and transfers with proper authorization
 */
public class TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private static final Logger transactionLogger = LoggerFactory.getLogger("TRANSACTION");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private AccountManager accountManager;
    private AuthenticationService authService;
    private static TransactionManager instance;

    private static final double MINIMUM_BALANCE = ConfigManager.getMinimumBalance();

    private TransactionManager() {
        this.accountManager = AccountManager.getInstance();
        this.authService = AuthenticationService.getInstance();
    }

    public static TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    /**
     * Process Deposit - With Access Control
     */
    public synchronized Transaction deposit(String accountId, double amount, String description)
            throws Exception {

        // ✅ Authorization Check
        authService.requireAuthentication();

        // Check if user can access this account
        if (!authService.canAccessAccount(accountId)) {
            logger.warn("Unauthorized deposit attempt on account: {} by user: {}",
                    accountId, authService.getCurrentUser().getUsername());
            auditLogger.warn("UNAUTHORIZED_TRANSACTION | Type: DEPOSIT | Account: {} | User: {} | DENIED",
                    accountId, authService.getCurrentUser().getUsername());
            throw new Exception("Access denied. You can only perform transactions on your own account.");
        }

        logger.info("Deposit initiated: Account: {} | Amount: {} | User: {}",
                accountId, amount, authService.getCurrentUser().getUsername());

        // Validate amount
        if (amount <= 0) {
            logger.warn("Invalid deposit amount: {}", amount);
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        // Get account
        Account account = accountManager.getAccount(accountId);

        // Update balance
        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        // Create transaction record
        Transaction transaction = new Transaction(
                accountId,
                TransactionType.DEPOSIT,
                amount,
                newBalance,
                description != null ? description : "Deposit"
        );

        // Save to database
        saveTransactionToDatabase(transaction);
        accountManager.updateBalance(accountId, newBalance);

        // Log transaction
        transactionLogger.info("DEPOSIT | Account: {} | Amount: {} | Balance: {} | User: {} | Time: {}",
                accountId, amount, newBalance,
                authService.getCurrentUser().getUsername(),
                LocalDateTime.now());

        auditLogger.info("TRANSACTION_SUCCESS | Type: DEPOSIT | Account: {} | Amount: {} | User: {} | Role: {}",
                accountId, amount, authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        logger.info("Deposit successful: ₹{} to account: {}", amount, accountId);
        System.out.println("✓ Deposit successful: ₹" + String.format("%.2f", amount));
        return transaction;
    }

    /**
     * Process Withdrawal - With Access Control
     */
    public synchronized Transaction withdraw(String accountId, double amount, String description)
            throws Exception {

        // ✅ Authorization Check
        authService.requireAuthentication();

        // Check if user can access this account
        if (!authService.canAccessAccount(accountId)) {
            logger.warn("Unauthorized withdrawal attempt on account: {} by user: {}",
                    accountId, authService.getCurrentUser().getUsername());
            auditLogger.warn("UNAUTHORIZED_TRANSACTION | Type: WITHDRAWAL | Account: {} | User: {} | DENIED",
                    accountId, authService.getCurrentUser().getUsername());
            throw new Exception("Access denied. You can only perform transactions on your own account.");
        }

        logger.info("Withdrawal initiated: Account: {} | Amount: {} | User: {}",
                accountId, amount, authService.getCurrentUser().getUsername());

        // Validate amount
        if (amount <= 0) {
            logger.warn("Invalid withdrawal amount: {}", amount);
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        // Get account
        Account account = accountManager.getAccount(accountId);

        // Check sufficient balance
        if (account.getBalance() < amount) {
            logger.warn("Insufficient funds: Account: {} | Available: {} | Requested: {}",
                    accountId, account.getBalance(), amount);
            auditLogger.warn("TRANSACTION_FAILED | Type: WITHDRAWAL | Account: {} | Reason: INSUFFICIENT_FUNDS | User: {}",
                    accountId, authService.getCurrentUser().getUsername());
            throw new InsufficientFundsException(
                    "Insufficient funds! Available: ₹" + String.format("%.2f", account.getBalance())
            );
        }

        // Check minimum balance after withdrawal
        double newBalance = account.getBalance() - amount;
        if (newBalance < MINIMUM_BALANCE) {
            logger.warn("Minimum balance violation: Account: {} | Resulting balance: {} | Min required: {}",
                    accountId, newBalance, MINIMUM_BALANCE);
            auditLogger.warn("TRANSACTION_FAILED | Type: WITHDRAWAL | Account: {} | Reason: MIN_BALANCE_VIOLATION | User: {}",
                    accountId, authService.getCurrentUser().getUsername());
            throw new InsufficientFundsException(
                    "Withdrawal would violate minimum balance requirement of ₹" + MINIMUM_BALANCE
            );
        }

        // Update balance
        account.setBalance(newBalance);

        // Create transaction record
        Transaction transaction = new Transaction(
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                newBalance,
                description != null ? description : "Withdrawal"
        );

        // Save to database
        saveTransactionToDatabase(transaction);
        accountManager.updateBalance(accountId, newBalance);

        // Log transaction
        transactionLogger.info("WITHDRAWAL | Account: {} | Amount: {} | Balance: {} | User: {} | Time: {}",
                accountId, amount, newBalance,
                authService.getCurrentUser().getUsername(),
                LocalDateTime.now());

        auditLogger.info("TRANSACTION_SUCCESS | Type: WITHDRAWAL | Account: {} | Amount: {} | User: {} | Role: {}",
                accountId, amount, authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        logger.info("Withdrawal successful: ₹{} from account: {}", amount, accountId);
        System.out.println("✓ Withdrawal successful: ₹" + String.format("%.2f", amount));
        return transaction;
    }

    /**
     * Process Transfer between accounts - With Access Control
     */
    public synchronized void transfer(String fromAccountId, String toAccountId,
                                      double amount, String description) throws Exception {

        // ✅ Authorization Check
        authService.requireAuthentication();

        // Check if user can access source account
        if (!authService.canAccessAccount(fromAccountId)) {
            logger.warn("Unauthorized transfer attempt from account: {} by user: {}",
                    fromAccountId, authService.getCurrentUser().getUsername());
            auditLogger.warn("UNAUTHORIZED_TRANSACTION | Type: TRANSFER | From: {} | User: {} | DENIED",
                    fromAccountId, authService.getCurrentUser().getUsername());
            throw new Exception("Access denied. You can only transfer from your own account.");
        }

        logger.info("Transfer initiated: From: {} | To: {} | Amount: {} | User: {}",
                fromAccountId, toAccountId, amount, authService.getCurrentUser().getUsername());

        // Validate amount
        if (amount <= 0) {
            logger.warn("Invalid transfer amount: {}", amount);
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        // Check if accounts are different
        if (fromAccountId.equals(toAccountId)) {
            logger.warn("Transfer to same account attempted: {}", fromAccountId);
            throw new InvalidAmountException("Cannot transfer to the same account");
        }

        // Get both accounts
        Account fromAccount = accountManager.getAccount(fromAccountId);

        // Verify destination account exists (admin/employee can see all, customer cannot)
        if (!accountManager.accountExists(toAccountId)) {
            logger.warn("Transfer to non-existent account: {}", toAccountId);
            throw new AccountNotFoundException("Destination account not found: " + toAccountId);
        }

        // For customers, we need to get the destination account using internal method
        // since they don't have permission to access other accounts directly
        Account toAccount;
        if (authService.isCustomer()) {
            // Customer cannot directly access other accounts, so we use internal method
            toAccount = accountManager.getAccountInternal(toAccountId);
        } else {
            // Admin/Employee can access any account
            toAccount = accountManager.getAccount(toAccountId);
        }

        // Check sufficient balance in source account
        if (fromAccount.getBalance() < amount) {
            logger.warn("Insufficient funds for transfer: From: {} | Available: {} | Requested: {}",
                    fromAccountId, fromAccount.getBalance(), amount);
            auditLogger.warn("TRANSACTION_FAILED | Type: TRANSFER | From: {} | Reason: INSUFFICIENT_FUNDS | User: {}",
                    fromAccountId, authService.getCurrentUser().getUsername());
            throw new InsufficientFundsException(
                    "Insufficient funds in source account! Available: ₹" +
                            String.format("%.2f", fromAccount.getBalance())
            );
        }

        // Check minimum balance after transfer
        double newFromBalance = fromAccount.getBalance() - amount;
        if (newFromBalance < MINIMUM_BALANCE) {
            logger.warn("Transfer would violate minimum balance: From: {} | Resulting: {} | Min: {}",
                    fromAccountId, newFromBalance, MINIMUM_BALANCE);
            auditLogger.warn("TRANSACTION_FAILED | Type: TRANSFER | From: {} | Reason: MIN_BALANCE_VIOLATION | User: {}",
                    fromAccountId, authService.getCurrentUser().getUsername());
            throw new InsufficientFundsException(
                    "Transfer would violate minimum balance requirement of ₹" + MINIMUM_BALANCE
            );
        }

        // Perform transfer
        double newToBalance = toAccount.getBalance() + amount;

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        // Create transaction records for both accounts
        String desc = description != null ? description : "Transfer";

        Transaction debitTransaction = new Transaction(
                fromAccountId,
                TransactionType.TRANSFER_OUT,
                amount,
                newFromBalance,
                desc + " to " + toAccountId,
                toAccountId
        );

        Transaction creditTransaction = new Transaction(
                toAccountId,
                TransactionType.TRANSFER_IN,
                amount,
                newToBalance,
                desc + " from " + fromAccountId,
                fromAccountId
        );

        // Save both transactions to database
        saveTransactionToDatabase(debitTransaction);
        saveTransactionToDatabase(creditTransaction);

        // Update both account balances
        accountManager.updateBalance(fromAccountId, newFromBalance);
        accountManager.updateBalance(toAccountId, newToBalance);

        // Log transaction
        transactionLogger.info("TRANSFER | From: {} | To: {} | Amount: {} | FromBalance: {} | ToBalance: {} | User: {} | Time: {}",
                fromAccountId, toAccountId, amount, newFromBalance, newToBalance,
                authService.getCurrentUser().getUsername(), LocalDateTime.now());

        auditLogger.info("TRANSACTION_SUCCESS | Type: TRANSFER | From: {} | To: {} | Amount: {} | User: {} | Role: {}",
                fromAccountId, toAccountId, amount,
                authService.getCurrentUser().getUsername(),
                authService.getCurrentUser().getRole());

        logger.info("Transfer successful: ₹{} from {} to {}", amount, fromAccountId, toAccountId);
        System.out.println("✓ Transfer successful: ₹" + String.format("%.2f", amount) +
                " from " + fromAccountId + " to " + toAccountId);
    }

    /**
     * Get transaction history for an account - With Access Control
     */
    public List<Transaction> getTransactionHistory(String accountId) throws Exception {
        // ✅ Authorization Check
        authService.requireAuthentication();

        // Check if user can access this account
        if (!authService.canAccessAccount(accountId)) {
            logger.warn("Unauthorized transaction history access: Account: {} by User: {}",
                    accountId, authService.getCurrentUser().getUsername());
            auditLogger.warn("UNAUTHORIZED_ACCESS | Resource: TRANSACTION_HISTORY | Account: {} | User: {} | DENIED",
                    accountId, authService.getCurrentUser().getUsername());
            throw new Exception("Access denied. You can only view your own transaction history.");
        }

        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getString("account_id"),
                        TransactionType.valueOf(rs.getString("transaction_type")),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getString("description"),
                        rs.getTimestamp("transaction_date").toLocalDateTime(),
                        TransactionStatus.valueOf(rs.getString("status"))
                );
                transactions.add(transaction);
            }

            logger.info("Transaction history retrieved: Account: {} | Count: {} | User: {}",
                    accountId, transactions.size(), authService.getCurrentUser().getUsername());
        } catch (SQLException e) {
            logger.error("Error retrieving transaction history for account: {}", accountId, e);
            throw e;
        }

        return transactions;
    }

    /**
     * Get all transactions - ADMIN/EMPLOYEE ONLY
     */
    public List<Transaction> getAllTransactions() throws Exception {
        // ✅ Authorization Check - Only Admin and Employee
        authService.requireEmployeeOrAdmin();

        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT 100";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getString("account_id"),
                        TransactionType.valueOf(rs.getString("transaction_type")),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getString("description"),
                        rs.getTimestamp("transaction_date").toLocalDateTime(),
                        TransactionStatus.valueOf(rs.getString("status"))
                );
                transactions.add(transaction);
            }

            logger.info("All transactions retrieved: Count: {} | User: {}",
                    transactions.size(), authService.getCurrentUser().getUsername());
        } catch (SQLException e) {
            logger.error("Error retrieving all transactions", e);
            throw e;
        }

        return transactions;
    }

    /**
     * Get transaction count for an account
     */
    public int getTransactionCount(String accountId) throws Exception {
        // ✅ Authorization Check
        authService.requireAuthentication();

        if (!authService.canAccessAccount(accountId)) {
            throw new Exception("Access denied");
        }

        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting transaction count for account: {}", accountId, e);
        }

        return 0;
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Save transaction to database
     */
    private void saveTransactionToDatabase(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                "balance_after, description, performed_by, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDouble(4, transaction.getBalanceAfter());
            stmt.setString(5, transaction.getDescription());

            // Track who performed this transaction
            if (authService.isLoggedIn()) {
                stmt.setInt(6, authService.getCurrentUser().getUserId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, transaction.getStatus().name());

            stmt.executeUpdate();

            // Get generated transaction ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setTransactionId(rs.getInt(1));
            }

            logger.debug("Transaction saved to database: ID: {}", transaction.getTransactionId());
        }
    }

    /**
     * Display transaction history - With Access Control
     */
    public void displayTransactionHistory(String accountId) throws Exception {
        List<Transaction> transactions = getTransactionHistory(accountId);

        System.out.println("\n========== TRANSACTION HISTORY ==========");
        System.out.println("Account: " + accountId);
        System.out.println("Total Transactions: " + transactions.size());
        System.out.println("=========================================");

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : transactions) {
                System.out.printf("ID: %d | %s | ₹%.2f | Balance: ₹%.2f | %s | %s\n",
                        t.getTransactionId(),
                        t.getTransactionType().getDisplayName(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getTransactionDate().toString(),
                        t.getDescription()
                );
            }
        }
        System.out.println("=========================================\n");
    }

    public static double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
}