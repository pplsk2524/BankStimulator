package main.java.com.banking.model;

public enum AccountType {
    SAVINGS("Savings Account"),
    CURRENT("Current Account"),
    FIXED_DEPOSIT("Fixed Deposit Account"),
    SALARY("Salary Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
