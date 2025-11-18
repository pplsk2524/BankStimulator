package main.java.com.banking.model;


/**
 * User Role Enumeration
 * Defines access levels in the banking system
 */
public enum UserRole {
    ADMIN("Administrator", "Full system access"),
    EMPLOYEE("Bank Employee", "Customer service and transactions"),
    CUSTOMER("Customer", "Own account access only");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
