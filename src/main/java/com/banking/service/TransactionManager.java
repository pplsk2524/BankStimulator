package main.java.com.banking.service;

import main.java.com.banking.model.*;
import main.java.com.banking.exception.*;
import main.java.com.banking.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Processing System
 * Handles deposits, withdrawals, and transfers
 */
public class TransactionManager {

    private AccountManager accountManager;
    private static TransactionManager instance;

    // Minimum balance required for accounts
    private static final double MINIMUM_BALANCE = 500.00;

    // Private constructor
    private TransactionManager() {
        this.accountManager = AccountManager.getInstance();
    }

    // Get singleton instance
    public static TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    /**
     * Process Deposit
     */
    public synchronized Transaction deposit(String accountId, double amount, String description)
            throws Exception {

        // Validate amount
        if (amount <= 0) {
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

        System.out.println("✓ Deposit successful: ₹" + String.format("%.2f", amount));
        return transaction;
    }

    /**
     * Process Withdrawal
     */
    public synchronized Transaction withdraw(String accountId, double amount, String description)
            throws Exception {

        // Validate amount
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        // Get account
        Account account = accountManager.getAccount(accountId);

        // Check sufficient balance
        if (account.getBalance() < amount) {
            throw new InsufficientFundsException(
                    "Insufficient funds! Available: ₹" + String.format("%.2f", account.getBalance())
            );
        }

        // Check minimum balance after withdrawal
        double newBalance = account.getBalance() - amount;
        if (newBalance < MINIMUM_BALANCE) {
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

        System.out.println("✓ Withdrawal successful: ₹" + String.format("%.2f", amount));
        return transaction;
    }

    /**
     * Process Transfer between accounts
     */
    public synchronized void transfer(String fromAccountId, String toAccountId,
                                      double amount, String description) throws Exception {

        // Validate amount
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        // Check if accounts are different
        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidAmountException("Cannot transfer to the same account");
        }

        // Get both accounts
        Account fromAccount = accountManager.getAccount(fromAccountId);
        Account toAccount = accountManager.getAccount(toAccountId);

        // Check sufficient balance in source account
        if (fromAccount.getBalance() < amount) {
            throw new InsufficientFundsException(
                    "Insufficient funds in source account! Available: ₹" +
                            String.format("%.2f", fromAccount.getBalance())
            );
        }

        // Check minimum balance after transfer
        double newFromBalance = fromAccount.getBalance() - amount;
        if (newFromBalance < MINIMUM_BALANCE) {
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

        System.out.println("✓ Transfer successful: ₹" + String.format("%.2f", amount) +
                " from " + fromAccountId + " to " + toAccountId);
    }

    /**
     * Get transaction history for an account
     */
    public List<Transaction> getTransactionHistory(String accountId) throws SQLException {
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
        }

        return transactions;
    }

    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() throws SQLException {
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
        }

        return transactions;
    }

    /**
     * Get transaction count for an account
     */
    public int getTransactionCount(String accountId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Save transaction to database
     */
    private void saveTransactionToDatabase(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                "balance_after, description, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDouble(4, transaction.getBalanceAfter());
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getStatus().name());

            stmt.executeUpdate();

            // Get generated transaction ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setTransactionId(rs.getInt(1));
            }
        }
    }

    /**
     * Display transaction history
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
