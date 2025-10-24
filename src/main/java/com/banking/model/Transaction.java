package main.java.com.banking.model;

import java.time.LocalDateTime;

/**
 * Transaction Model Class
 * Represents a banking transaction
 */
public class Transaction {
    private int transactionId;
    private String accountId;
    private TransactionType transactionType;
    private double amount;
    private double balanceAfter;
    private String description;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private String referenceAccountId; // For transfers

    // Constructor for Deposit/Withdrawal
    public Transaction(String accountId, TransactionType transactionType,
                       double amount, double balanceAfter, String description) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
        this.status = TransactionStatus.SUCCESS;
    }

    // Constructor for Transfer
    public Transaction(String accountId, TransactionType transactionType,
                       double amount, double balanceAfter, String description,
                       String referenceAccountId) {
        this(accountId, transactionType, amount, balanceAfter, description);
        this.referenceAccountId = referenceAccountId;
    }

    // Constructor for loading from database
    public Transaction(int transactionId, String accountId, TransactionType transactionType,
                       double amount, double balanceAfter, String description,
                       LocalDateTime transactionDate, TransactionStatus status) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getReferenceAccountId() {
        return referenceAccountId;
    }

    public void setReferenceAccountId(String referenceAccountId) {
        this.referenceAccountId = referenceAccountId;
    }

    @Override
    public String toString() {
        return String.format("Transaction[ID=%d, Type=%s, Amount=%.2f, Balance=%.2f, Date=%s, Status=%s]",
                transactionId, transactionType, amount, balanceAfter, transactionDate, status);
    }
}
