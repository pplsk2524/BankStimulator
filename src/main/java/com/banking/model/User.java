package main.java.com.banking.model;

//package com.banking.model;

import java.time.LocalDateTime;

/**
 * User Model for Authentication
 * Represents system users (Admin, Employee, Customer)
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String fullName;
    private String email;
    private String phone;
    private String linkedAccountId; // For customers - their account ID
    private boolean isActive;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdDate;

    // Constructor
    public User(String username, String passwordHash, UserRole role, String fullName,
                String email, String phone) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
        this.failedLoginAttempts = 0;
        this.createdDate = LocalDateTime.now();
    }

    // Full constructor (for database loading)
    public User(int userId, String username, String passwordHash, UserRole role,
                String fullName, String email, String phone, String linkedAccountId,
                boolean isActive, int failedLoginAttempts, LocalDateTime lastLoginTime,
                LocalDateTime createdDate) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.linkedAccountId = linkedAccountId;
        this.isActive = isActive;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastLoginTime = lastLoginTime;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getLinkedAccountId() {
        return linkedAccountId;
    }

    public void setLinkedAccountId(String linkedAccountId) {
        this.linkedAccountId = linkedAccountId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    public boolean isEmployee() {
        return role == UserRole.EMPLOYEE;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return String.format("User[ID=%d, Username=%s, Role=%s, Name=%s, Active=%s]",
                userId, username, role, fullName, isActive);
    }
}
