package main.java.com.banking.model;

import java.time.LocalDateTime;

/**
 * Account Model Class
 * Represents a bank account with all necessary attributes
 */
public class Account {
    private String accountId;
    private String holderName;
    private double balance;
    private AccountType accountType;
    private String email;
    private String phone;
    private LocalDateTime createdDate;
    private AccountStatus status;

    // Constructor
    public Account(String accountId, String holderName, double initialBalance,
                   AccountType accountType, String email, String phone) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.email = email;
        this.phone = phone;
        this.createdDate = LocalDateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    // Utility Methods
    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        this.balance -= amount;
    }

    @Override
    public String toString() {
        return String.format("Account[ID=%s, Holder=%s, Balance=%.2f, Type=%s, Status=%s]",
                accountId, holderName, balance, accountType, status);
    }
}

