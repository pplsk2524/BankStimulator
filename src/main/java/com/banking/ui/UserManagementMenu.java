package main.java.com.banking.ui;

import main.java.com.banking.model.Account;
import main.java.com.banking.model.User;
import main.java.com.banking.model.UserRole;
import main.java.com.banking.service.AccountManager;
import main.java.com.banking.service.AuthenticationService;
import main.java.com.banking.service.UserManager;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserManagementMenu {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementMenu.class);
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final AccountManager accountManager;
    private final UserManager userManager;
    private final User currentUser;

    public UserManagementMenu(Scanner scanner, User currentUser) {
        this.scanner = scanner;
        this.authService = AuthenticationService.getInstance();
        this.accountManager = AccountManager.getInstance();
        this.userManager = UserManager.getInstance();
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║       USER MANAGEMENT MENU             ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. 🔗 Link Existing Accounts to Users (Auto)");
            System.out.println("2. 👤 Create User for Specific Account");
            System.out.println("3. 📋 View All Users");
            System.out.println("4. 👨‍💼 Create Staff/Admin User");
            System.out.println("5. 🔐 Reset User Password");
            System.out.println("6. 🔓 Unlock User Account");
            System.out.println("7. 📊 View Accounts Without Users");
            System.out.println("8. ⬅️  Back to Main Menu");
            System.out.println("═══════════════════════════════════════════");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> autoLinkAllAccounts();
                    case "2" -> createUserForSpecificAccount();
                    case "3" -> viewAllUsers();
                    case "4" -> createStaffUser();
                    case "5" -> resetPassword();
                    case "6" -> unlockAccount();
                    case "7" -> viewAccountsWithoutUsers();
                    case "8" -> {
                        return;
                    }
                    default -> System.out.println("❌ Invalid choice! Please enter 1-8.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                logger.error("Error in user management menu", e);
            }
        }
    }

    /**
     * Auto-link all accounts without users
     */
    private void autoLinkAllAccounts() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   AUTO-LINK ACCOUNTS TO USER CREDENTIALS      ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        List<Account> unlinkedAccounts = getAccountsWithoutUsers();

        if (unlinkedAccounts.isEmpty()) {
            System.out.println("✓ All accounts already have user credentials!");
            return;
        }

        System.out.println("\n📋 Found " + unlinkedAccounts.size() + " account(s) without users:");
        System.out.println("─────────────────────────────────────────────────");
        for (Account acc : unlinkedAccounts) {
            System.out.printf("  • ID: %-10s | Name: %s%n",
                    acc.getAccountId(), acc.getHolderName());
        }
        System.out.println("─────────────────────────────────────────────────");

        System.out.println("\n⚙️  Auto-configuration:");
        System.out.println("   • Username format: account{ID} (e.g., account" +
                unlinkedAccounts.get(0).getAccountId() + ")");
        System.out.println("   • Default password: Welcome@123");
        System.out.println("   • Role: CUSTOMER");
        System.out.println("   • Status: Active");

        System.out.print("\n⚠️  Proceed with auto-linking? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("❌ Operation cancelled.");
            return;
        }

        System.out.println("\n🔄 Creating user credentials...\n");

        int successCount = 0;
        int failCount = 0;
        List<String> createdCredentials = new ArrayList<>();

        for (Account acc : unlinkedAccounts) {
            String username = "account" + acc.getAccountId();
            String tempPassword = "Welcome@123";

            try {
                userManager.createCustomerUser(
                        acc.getAccountId(),  // Fixed: Already a String
                        username,
                        tempPassword
                );
                createdCredentials.add(username);
                successCount++;

            } catch (Exception e) {
                System.out.println("❌ Failed for " + acc.getHolderName() + ": " + e.getMessage());
                failCount++;
                logger.error("Failed to create user for account: " + acc.getAccountId(), e);
            }
        }

        // Summary
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              OPERATION SUMMARY                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("✓ Successfully created: " + successCount + " user(s)");
        if (failCount > 0) {
            System.out.println("❌ Failed: " + failCount + " user(s)");
        }

        if (!createdCredentials.isEmpty()) {
            System.out.println("\n📧 Created Credentials:");
            System.out.println("─────────────────────────────────────────────────");
            for (String username : createdCredentials) {
                System.out.println("   Username: " + username);
                System.out.println("   Password: Welcome@123");
                System.out.println("   ─────────────────────");
            }
            System.out.println("\n⚠️  IMPORTANT: Users should change their password on first login!");
        }
        System.out.println("═══════════════════════════════════════════════════");
    }

    /**
     * Create user for a specific account
     */
    private void createUserForSpecificAccount() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     CREATE USER FOR SPECIFIC ACCOUNT           ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        List<Account> unlinkedAccounts = getAccountsWithoutUsers();

        if (unlinkedAccounts.isEmpty()) {
            System.out.println("✓ All accounts already have user credentials!");
            return;
        }

        System.out.println("\n📋 Accounts without user credentials:");
        System.out.println("─────────────────────────────────────────────────");
        for (Account acc : unlinkedAccounts) {
            System.out.printf("  ID: %-10s | Name: %-25s | Email: %s%n",
                    acc.getAccountId(),
                    truncate(acc.getHolderName(), 25),
                    acc.getEmail());
        }
        System.out.println("─────────────────────────────────────────────────");

        System.out.print("\nEnter Account ID (or 'cancel' to go back): ");
        String accountIdStr = scanner.nextLine().trim();

        if (accountIdStr.equalsIgnoreCase("cancel")) {
            return;
        }

        // Find account in the unlinked list
        Account account = null;
        for (Account acc : unlinkedAccounts) {
            if (acc.getAccountId().equals(accountIdStr)) {
                account = acc;
                break;
            }
        }

        if (account == null) {
            System.out.println("❌ Account not found or already has a user!");
            return;
        }

        System.out.println("\n📋 Creating credentials for: " + account.getHolderName());
        System.out.println("   Account ID: " + account.getAccountId());
        System.out.println("   Email: " + account.getEmail());

        System.out.print("\nEnter username (min 3 characters): ");
        String username = scanner.nextLine().trim();

        if (username.length() < 3) {
            System.out.println("❌ Username must be at least 3 characters!");
            return;
        }

        System.out.print("Enter temporary password (min 8 chars, 1 upper, 1 lower, 1 digit, 1 special): ");
        String password = scanner.nextLine().trim();

        try {
            userManager.createCustomerUser(
                    account.getAccountId(),  // Fixed: Already a String
                    username,
                    password
            );
            // Success message already printed by UserManager

        } catch (Exception e) {
            System.out.println("❌ Failed to create user: " + e.getMessage());
            logger.error("Error creating user", e);
        }
    }

    /**
     * View all users in the system
     */
    private void viewAllUsers() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                            ALL USERS IN SYSTEM                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, username, role, full_name, linked_account_id, is_active, " +
                             "failed_login_attempts, DATE(created_date) as created_date FROM users ORDER BY user_id")) {

            ResultSet rs = stmt.executeQuery();

            System.out.println();
            System.out.println("┌──────┬───────────────┬──────────┬─────────────────────┬────────────┬────────┬──────────┐");
            System.out.println("│ ID   │ Username      │ Role     │ Full Name           │ Account ID │ Active │ Failed   │");
            System.out.println("├──────┼───────────────┼──────────┼─────────────────────┼────────────┼────────┼──────────┤");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("│ %-4d │ %-13s │ %-8s │ %-19s │ %-10s │ %-6s │ %-8d │%n",
                        rs.getInt("user_id"),
                        truncate(rs.getString("username"), 13),
                        rs.getString("role"),
                        truncate(rs.getString("full_name"), 19),
                        rs.getString("linked_account_id") != null ?
                                rs.getString("linked_account_id") : "N/A",
                        rs.getBoolean("is_active") ? "Yes" : "No",
                        rs.getInt("failed_login_attempts")
                );
            }

            System.out.println("└──────┴───────────────┴──────────┴─────────────────────┴────────────┴────────┴──────────┘");
            System.out.println("\nTotal users: " + count);

        } catch (Exception e) {
            System.out.println("❌ Error retrieving users: " + e.getMessage());
            logger.error("Error viewing users", e);
        }
    }

    /**
     * Create staff or admin user (not linked to account)
     */
    private void createStaffUser() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║        CREATE STAFF/ADMIN USER                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        System.out.println("\nℹ️  This feature creates staff/admin users not linked to customer accounts.");
        System.out.println("⚠️  For customer accounts, use option 1 or 2 instead.\n");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter phone: ");
        String phone = scanner.nextLine().trim();

        System.out.println("\nSelect role:");
        System.out.println("1. STAFF");
        System.out.println("2. ADMIN");
        System.out.print("Choice: ");
        String roleChoice = scanner.nextLine().trim();

        UserRole role = roleChoice.equals("2") ? UserRole.ADMIN : UserRole.EMPLOYEE;

        System.out.println("\n⚠️  This functionality requires extending UserManager.");
        System.out.println("Current UserManager only supports creating CUSTOMER users.");
        System.out.println("You can manually insert into database or extend the UserManager class.");
    }

    /**
     * Reset user password
     */
    private void resetPassword() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║           RESET USER PASSWORD                  ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        System.out.print("\nEnter username to reset: ");
        String username = scanner.nextLine().trim();

        System.out.print("⚠️  Are you sure you want to reset password for '" + username + "'? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("❌ Operation cancelled.");
            return;
        }

        try {
            String tempPassword = userManager.resetUserPassword(username);
            // Success message and temp password already shown by UserManager

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            logger.error("Error resetting password", e);
        }
    }

    /**
     * Unlock user account
     */
    private void unlockAccount() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║           UNLOCK USER ACCOUNT                  ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        System.out.print("\nEnter username to unlock: ");
        String username = scanner.nextLine().trim();

        try {
            userManager.unlockUserAccount(username);
            // Success message already shown by UserManager

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            logger.error("Error unlocking account", e);
        }
    }

    /**
     * View accounts without users
     */
    private void viewAccountsWithoutUsers() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ACCOUNTS WITHOUT USER CREDENTIALS                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");

        List<Account> unlinkedAccounts = getAccountsWithoutUsers();

        if (unlinkedAccounts.isEmpty()) {
            System.out.println("\n✓ All accounts have user credentials!");
            return;
        }

        System.out.println("\n📋 Found " + unlinkedAccounts.size() + " account(s) without users:\n");
        System.out.println("┌────────────┬─────────────────────────────┬───────────────────────────┬──────────────┐");
        System.out.println("│ Account ID │ Account Holder Name         │ Email                     │ Balance      │");
        System.out.println("├────────────┼─────────────────────────────┼───────────────────────────┼──────────────┤");

        for (Account acc : unlinkedAccounts) {
            System.out.printf("│ %-10s │ %-27s │ %-25s │ $%-11.2f │%n",
                    acc.getAccountId(),  // Fixed: Use %s instead of %d
                    truncate(acc.getHolderName(), 27),
                    truncate(acc.getEmail(), 25),
                    acc.getBalance()
            );
        }

        System.out.println("└────────────┴─────────────────────────────┴───────────────────────────┴──────────────┘");
        System.out.println("\n💡 Tip: Use option 1 to auto-link all accounts or option 2 for individual setup.");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get all accounts that don't have associated users
     */
    private List<Account> getAccountsWithoutUsers() {
        List<Account> unlinkedAccounts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.* FROM accounts a " +
                             "LEFT JOIN users u ON a.account_id = u.linked_account_id " +
                             "WHERE u.user_id IS NULL AND a.status = 'ACTIVE'")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                try {
                    // Create Account object directly from ResultSet to avoid authorization issues
                    String accountId = rs.getString("account_id");
                    String holderName = rs.getString("holder_name");
                    double balance = rs.getDouble("balance");
                    String accountTypeStr = rs.getString("account_type");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");

                    // Create account object directly without going through AccountManager
                    Account account = new Account(
                            accountId,
                            holderName,
                            balance,
                            main.java.com.banking.model.AccountType.valueOf(accountTypeStr),
                            email,
                            phone
                    );
                    unlinkedAccounts.add(account);

                } catch (Exception e) {
                    logger.warn("Could not load account: " + rs.getString("account_id"), e);
                    // Continue processing other accounts
                }
            }

        } catch (Exception e) {
            logger.error("Error getting accounts without users", e);
        }

        return unlinkedAccounts;
    }

    /**
     * Truncate string to specified length
     */
    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}