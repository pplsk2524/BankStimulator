package main.java.com.banking;

import main.java.com.banking.model.*;
import main.java.com.banking.service.*;
import main.java.com.banking.util.DatabaseConfig;
import main.java.com.banking.ui.UserManagementMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main Application - Banking Transaction Simulator
 * With Authentication and Role-Based Access Control
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static AccountManager accountManager;
    private static TransactionManager transactionManager;
    private static ReportGenerator reportGenerator;
    private static EmailService emailService;
    private static BalanceAlertTracker alertTracker;
    private static AuthenticationService authService;
    private static UserManager userManager;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Display welcome banner
        displayWelcomeBanner();

        // Test database connection
        if (!DatabaseConfig.testConnection()) {
            System.err.println("❌ Failed to connect to database. Please check your configuration.");
            logger.error("Database connection failed on startup");
            return;
        }

        // Initialize all services
        initializeServices();

        // Start balance monitoring
        alertTracker.startMonitoring();

        // Main application loop
        boolean running = true;
        while (running) {

            // Login screen
            if (!authService.isLoggedIn()) {
                if (!showLoginScreen()) {
                    running = false;
                    continue;
                }
            }

            // Show role-specific menu
            try {
                if (authService.isAdmin()) {
                    adminMenu();
                } else if (authService.isEmployee()) {
                    employeeMenu();
                } else if (authService.isCustomer()) {
                    customerMenu();
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                logger.error("Error in menu", e);
            }
        }

        // Cleanup
        cleanup();
        scanner.close();
    }

    /**
     * Display welcome banner
     */
    private static void displayWelcomeBanner() {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║     BANKING TRANSACTION SIMULATOR v2.0            ║");
        System.out.println("║     With Authentication & Access Control          ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
        logger.info("Application started");
    }

    /**
     * Initialize all services
     */
    private static void initializeServices() {
        accountManager = AccountManager.getInstance();
        transactionManager = TransactionManager.getInstance();
        reportGenerator = new ReportGenerator();
        emailService = EmailService.getInstance();
        alertTracker = BalanceAlertTracker.getInstance();
        authService = AuthenticationService.getInstance();
        userManager=UserManager.getInstance();

        logger.info("All services initialized successfully");
    }

    /**
     * Show login screen
     */
    private static boolean showLoginScreen() {
        System.out.println("\n========== LOGIN ==========");
        System.out.println("Enter 'exit' as username to quit");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.equalsIgnoreCase("exit")) {
            System.out.println("\nGoodbye!");
            logger.info("Application exit requested from login screen");
            return false;
        }

        if (username.isEmpty()) {
            System.out.println("❌ Username cannot be empty\n");
            return true;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            authService.login(username, password);
            return true;
        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage() + "\n");
            return true;
        }
    }

    // ==================== ADMIN MENU ====================

    private static void adminMenu() throws Exception {
        while (authService.isLoggedIn()) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("Logged in as: " + authService.getCurrentUser().getFullName());
            System.out.println("1.  Account Management");
            System.out.println("2.  Transaction Management");
            System.out.println("3.  Reports & Analytics");
            System.out.println("4.  Alerts & Monitoring");
            System.out.println("5.  User Management");
            System.out.println("6.  System Statistics");
            System.out.println("7.  Logout");
            System.out.println("================================");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    adminAccountManagement();
                    break;
                case 2:
                    adminTransactionManagement();
                    break;
                case 3:
                    reportsMenu();
                    break;
                case 4:
                    alertsMenu();
                    break;
                case 5:
                    userManagementMenu();
                    break;
                case 6:
                    displayStatistics();
                    break;
                case 7:
                    authService.logout();
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    private static void adminAccountManagement() throws Exception {
        while (true) {
            System.out.println("\n--- ACCOUNT MANAGEMENT ---");
            System.out.println("1. Create Account");
            System.out.println("2. View Account");
            System.out.println("3. View All Accounts");
            System.out.println("4. Delete Account");
            System.out.println("5. Back");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    createAccountMenu();
                    break;
                case 2:
                    viewAccountMenu();
                    break;
                case 3:
                    accountManager.displayAllAccounts();
                    break;
                case 4:
                    deleteAccountMenu();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    private static void adminTransactionManagement() throws Exception {
        while (true) {
            System.out.println("\n--- TRANSACTION MANAGEMENT ---");
            System.out.println("1. Process Deposit");
            System.out.println("2. Process Withdrawal");
            System.out.println("3. Process Transfer");
            System.out.println("4. View Transaction History");
            System.out.println("5. View All Transactions");
            System.out.println("6. Back");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    depositMenu();
                    break;
                case 2:
                    withdrawMenu();
                    break;
                case 3:
                    transferMenu();
                    break;
                case 4:
                    viewTransactionHistoryMenu();
                    break;
                case 5:
                    viewAllTransactionsMenu();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    // ==================== EMPLOYEE MENU ====================

    private static void employeeMenu() throws Exception {
        while (authService.isLoggedIn()) {
            System.out.println("\n========== EMPLOYEE MENU ==========");
            System.out.println("Logged in as: " + authService.getCurrentUser().getFullName());
            System.out.println("1. View All Accounts");
            System.out.println("2. Process Transactions");
            System.out.println("3. View Transaction History");
            System.out.println("4. Generate Reports");
            System.out.println("5. Customer Service");
            System.out.println("6. Logout");
            System.out.println("===================================");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    accountManager.displayAllAccounts();
                    break;
                case 2:
                    employeeTransactionMenu();
                    break;
                case 3:
                    viewTransactionHistoryMenu();
                    break;
                case 4:
                    reportsMenu();
                    break;
                case 5:
                    customerServiceMenu();
                    break;
                case 6:
                    authService.logout();
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    private static void employeeTransactionMenu() throws Exception {
        while (true) {
            System.out.println("\n--- PROCESS TRANSACTIONS ---");
            System.out.println("1. Deposit");
            System.out.println("2. Withdrawal");
            System.out.println("3. Transfer");
            System.out.println("4. Back");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    depositMenu();
                    break;
                case 2:
                    withdrawMenu();
                    break;
                case 3:
                    transferMenu();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    private static void customerServiceMenu() throws Exception {
        while (true) {
            System.out.println("\n--- CUSTOMER SERVICE ---");
            System.out.println("1. Search Account");
            System.out.println("2. Account Summary");
            System.out.println("3. Check Balance Alerts");
            System.out.println("4. Back");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    viewAccountMenu();
                    break;
                case 2:
                    viewAccountMenu();
                    break;
                case 3:
                    alertTracker.checkAllAccountBalances();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    // ==================== CUSTOMER MENU ====================

    private static void customerMenu() throws Exception {
        while (authService.isLoggedIn()) {
            System.out.println("\n========== CUSTOMER MENU ==========");
            System.out.println("Logged in as: " + authService.getCurrentUser().getFullName());
            System.out.println("Account: " + authService.getCurrentUser().getLinkedAccountId());
            System.out.println("1. View My Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. View Transaction History");
            System.out.println("6. Check Balance Alert");
            System.out.println("7.Change password");
            System.out.println("8. Logout");
            System.out.println("===================================");

            int choice = getIntInput("Enter choice: ");

            String myAccountId = authService.getCurrentUser().getLinkedAccountId();

            switch (choice) {
                case 1:
                    if (myAccountId != null) {
                        viewSpecificAccount(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 2:
                    if (myAccountId != null) {
                        customerDepositMenu(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 3:
                    if (myAccountId != null) {
                        customerWithdrawMenu(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 4:
                    if (myAccountId != null) {
                        customerTransferMenu(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 5:
                    if (myAccountId != null) {
                        transactionManager.displayTransactionHistory(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 6:
                    if (myAccountId != null) {
                        alertTracker.checkAccountBalance(myAccountId);
                    } else {
                        System.out.println("❌ No account linked to your profile");
                    }
                    break;
                case 7:
                    changePasswordMenu();
                    break;
                    
                case 8:
                    authService.logout();
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    // ==================== ACCOUNT OPERATIONS ====================

    private static void createAccountMenu() {
        try {
            System.out.println("\n--- Create New Account ---");
            System.out.println("Format Requirements:");
            System.out.println("• Account ID: ACC followed by 3-6 digits (e.g., ACC001)");
            System.out.println("• Name: 3-50 characters, letters only");
            System.out.println("• Email: user@domain.com");
            System.out.println("• Phone: 10 digits starting with 6-9\n");

            System.out.print("Enter Account ID (e.g., ACC001): ");
            String accountId = scanner.nextLine().trim();

            System.out.print("Enter Holder Name: ");
            String holderName = scanner.nextLine().trim();

            double initialBalance = getDoubleInput("Enter Initial Balance: ");

            System.out.println("\nAccount Types:");
            System.out.println("1. SAVINGS");
            System.out.println("2. CURRENT");
            System.out.println("3. FIXED_DEPOSIT");
            System.out.println("4. SALARY");
            int typeChoice = getIntInput("Select Account Type: ");

            AccountType accountType;
            switch (typeChoice) {
                case 1:
                    accountType = AccountType.SAVINGS;
                    break;
                case 2:
                    accountType = AccountType.CURRENT;
                    break;
                case 3:
                    accountType = AccountType.FIXED_DEPOSIT;
                    break;
                case 4:
                    accountType = AccountType.SALARY;
                    break;
                default:
                    System.out.println("❌ Invalid type. Defaulting to SAVINGS");
                    accountType = AccountType.SAVINGS;
            }

            System.out.print("Enter Email (e.g., user@example.com): ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Phone (10 digits): ");
            String phone = scanner.nextLine().trim();

            Account account = accountManager.createAccount(
                    accountId, holderName, initialBalance, accountType, email, phone
            );

            System.out.println("\n✓ Account created successfully!");
            System.out.println(account);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            logger.error("Error creating account", e);
        }
    }

    private static void viewAccountMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

            viewSpecificAccount(accountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void viewSpecificAccount(String accountId) throws Exception {
        Account account = accountManager.getAccount(accountId);

        System.out.println("\n========== ACCOUNT DETAILS ==========");
        System.out.println("Account ID    : " + account.getAccountId());
        System.out.println("Holder Name   : " + account.getHolderName());
        System.out.println("Balance       : ₹" + String.format("%.2f", account.getBalance()));
        System.out.println("Account Type  : " + account.getAccountType().getDisplayName());
        System.out.println("Email         : " + account.getEmail());
        System.out.println("Phone         : " + account.getPhone());
        System.out.println("Status        : " + account.getStatus());
        System.out.println("Created Date  : " + account.getCreatedDate());
        System.out.println("====================================");

        int txnCount = transactionManager.getTransactionCount(accountId);
        System.out.println("Total Transactions: " + txnCount);
    }

    private static void deleteAccountMenu() {
        try {
            System.out.print("\nEnter Account ID to delete: ");
            String accountId = scanner.nextLine().trim();

            System.out.print("⚠️  Are you sure? This cannot be undone. (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                accountManager.deleteAccount(accountId);
            } else {
                System.out.println("❌ Deletion cancelled.");
            }

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    // ==================== TRANSACTION OPERATIONS ====================

    private static void depositMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

            double amount = getDoubleInput("Enter deposit amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.deposit(accountId, amount, description);

            Account account = accountManager.getAccount(accountId);
            System.out.println("New Balance: ₹" + String.format("%.2f", account.getBalance()));

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void customerDepositMenu(String accountId) {
        try {
            System.out.println("\n--- Deposit to My Account ---");
            System.out.println("Account: " + accountId);

            double amount = getDoubleInput("Enter deposit amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.deposit(accountId, amount, description);

            Account account = accountManager.getAccount(accountId);
            System.out.println("New Balance: ₹" + String.format("%.2f", account.getBalance()));

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void withdrawMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

            Account account = accountManager.getAccount(accountId);
            System.out.println("Current Balance: ₹" + String.format("%.2f", account.getBalance()));
            System.out.println("Minimum Balance Required: ₹" + TransactionManager.getMinimumBalance());

            double amount = getDoubleInput("Enter withdrawal amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.withdraw(accountId, amount, description);

            account = accountManager.getAccount(accountId);
            System.out.println("New Balance: ₹" + String.format("%.2f", account.getBalance()));

            alertTracker.checkAccountBalance(accountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void customerWithdrawMenu(String accountId) {
        try {
            System.out.println("\n--- Withdraw from My Account ---");
            System.out.println("Account: " + accountId);

            Account account = accountManager.getAccount(accountId);
            System.out.println("Current Balance: ₹" + String.format("%.2f", account.getBalance()));
            System.out.println("Minimum Balance Required: ₹" + TransactionManager.getMinimumBalance());

            double amount = getDoubleInput("Enter withdrawal amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.withdraw(accountId, amount, description);

            account = accountManager.getAccount(accountId);
            System.out.println("New Balance: ₹" + String.format("%.2f", account.getBalance()));

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void transferMenu() {
        try {
            System.out.print("\nEnter Source Account ID: ");
            String fromAccountId = scanner.nextLine().trim();

            System.out.print("Enter Destination Account ID: ");
            String toAccountId = scanner.nextLine().trim();

            Account fromAccount = accountManager.getAccount(fromAccountId);
            System.out.println("Source Balance: ₹" + String.format("%.2f", fromAccount.getBalance()));

            double amount = getDoubleInput("Enter transfer amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.transfer(fromAccountId, toAccountId, amount, description);

            fromAccount = accountManager.getAccount(fromAccountId);
            System.out.println("\nSource Account Balance: ₹" + String.format("%.2f", fromAccount.getBalance()));

            alertTracker.checkAccountBalance(fromAccountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void customerTransferMenu(String fromAccountId) {
        try {
            System.out.println("\n--- Transfer from My Account ---");
            System.out.println("From Account: " + fromAccountId);

            System.out.print("Enter Destination Account ID: ");
            String toAccountId = scanner.nextLine().trim();

            Account fromAccount = accountManager.getAccount(fromAccountId);
            System.out.println("Your Balance: ₹" + String.format("%.2f", fromAccount.getBalance()));

            double amount = getDoubleInput("Enter transfer amount: ");

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;

            transactionManager.transfer(fromAccountId, toAccountId, amount, description);

            fromAccount = accountManager.getAccount(fromAccountId);
            System.out.println("New Balance: ₹" + String.format("%.2f", fromAccount.getBalance()));

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void viewTransactionHistoryMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

            transactionManager.displayTransactionHistory(accountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private static void viewAllTransactionsMenu() {
        try {
            var transactions = transactionManager.getAllTransactions();

            System.out.println("\n========== ALL RECENT TRANSACTIONS ==========");
            System.out.println("Total: " + transactions.size() + " (Last 100)");
            System.out.println("============================================");

            for (Transaction t : transactions) {
                System.out.printf("ID: %d | Account: %s | %s | ₹%.2f | %s\n",
                        t.getTransactionId(),
                        t.getAccountId(),
                        t.getTransactionType().getDisplayName(),
                        t.getAmount(),
                        t.getTransactionDate()
                );
            }
            System.out.println("============================================\n");

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    // ==================== REPORTS MENU ====================

    private static void reportsMenu() {
        while (true) {
            System.out.println("\n--- REPORTS & ANALYTICS ---");
            System.out.println("1. Account Summary Report");
            System.out.println("2. Transaction Report (by Account)");
            System.out.println("3. All Transactions Report");
            System.out.println("4. Low Balance Report");
            System.out.println("5. Export Accounts to CSV");
            System.out.println("6. Back");

            int choice = getIntInput("Enter choice: ");

            try {
                switch (choice) {
                    case 1:
                        reportGenerator.generateAccountSummaryReport();
                        break;
                    case 2:
                        System.out.print("Enter Account ID: ");
                        String accId = scanner.nextLine().trim();
                        reportGenerator.generateTransactionReport(accId);
                        break;
                    case 3:
                        reportGenerator.generateAllTransactionsReport();
                        break;
                    case 4:
                        double threshold = getDoubleInput("Enter threshold amount: ");
                        reportGenerator.generateLowBalanceReport(threshold);
                        break;
                    case 5:
                        reportGenerator.exportAccountsToCSV();
                        break;
                    case 6:
                        return;
                    default:
                        System.out.println("❌ Invalid choice");
                }
            } catch (Exception e) {
                System.err.println("❌ Error generating report: " + e.getMessage());
            }
        }
    }

    // ==================== ALERTS MENU ====================

    private static void alertsMenu() {
        while (true) {
            System.out.println("\n--- ALERTS & MONITORING ---");
            System.out.println("1. Check All Account Balances");
            System.out.println("2. Check Specific Account");
            System.out.println("3. View Alert Statistics");
            System.out.println("4. Start/Stop Monitoring");
            System.out.println("5. Test Email Configuration");
            System.out.println("6. Back");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    alertTracker.checkAllAccountBalances();
                    break;
                case 2:
                    System.out.print("Enter Account ID: ");
                    String accId = scanner.nextLine().trim();
                    alertTracker.checkAccountBalance(accId);
                    break;
                case 3:
                    alertTracker.displayAlertStatistics();
                    break;
                case 4:
                    toggleMonitoring();
                    break;
                case 5:
                    emailService.testEmailConfiguration();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }

    // ==================== USER MANAGEMENT ====================

    private static void userManagementMenu() {
        try {
            UserManagementMenu userMenu = new UserManagementMenu(scanner, authService.getCurrentUser());
            userMenu.show();
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    // ==================== STATISTICS ====================

    private static void displayStatistics() {
        try {
            System.out.println("\n========== SYSTEM STATISTICS ==========");
            System.out.println("Total Accounts: " + accountManager.getTotalAccounts());

            double totalBalance = 0;
            for (Account account : accountManager.getAllAccounts()) {
                totalBalance += account.getBalance();
            }
            System.out.println("Total Balance : ₹" + String.format("%.2f", totalBalance));

            if (accountManager.getTotalAccounts() > 0) {
                System.out.println("Average Balance: ₹" + String.format("%.2f",
                        totalBalance / accountManager.getTotalAccounts()));
            }

            System.out.println("\nAlert Statistics:");
            System.out.println("Low Balance Accounts: " + alertTracker.getLowBalanceAccounts().size());
            System.out.println("Critical Balance Accounts: " + alertTracker.getCriticalBalanceAccounts().size());
            System.out.println("Monitoring Status: " + (alertTracker.isMonitoring() ? "ACTIVE" : "INACTIVE"));

            System.out.println("\nCurrent User:");
            System.out.println("Username: " + authService.getCurrentUser().getUsername());
            System.out.println("Role: " + authService.getCurrentUser().getRole().getDisplayName());
            System.out.println("Session Duration: " + authService.getSessionDuration() + " minutes");

            System.out.println("=======================================\n");
        } catch (Exception e) {
            System.err.println("❌ Error displaying statistics: " + e.getMessage());
        }
    }

    /**
     * Change password menu for current user
     */
    private static void changePasswordMenu() {
        try {
            System.out.println("\n========== CHANGE PASSWORD ==========");

            System.out.print("Enter current password: ");
            String currentPassword = scanner.nextLine().trim();

            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();

            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine().trim();

            if (!newPassword.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match!");
                return;
            }

            // This will call your UserManager.changePassword() correctly
            userManager.changePassword(currentPassword, newPassword);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            logger.error("Error changing password", e);
        }
    }

    // ==================== UTILITY METHODS ====================

    private static void toggleMonitoring() {
        if (alertTracker.isMonitoring()) {
            alertTracker.stopMonitoring();
        } else {
            alertTracker.startMonitoring();
        }
    }

    private static void cleanup() {
        alertTracker.stopMonitoring();
        DatabaseConfig.closeConnection();
        logger.info("Application shutdown complete");
        System.out.println("\n✓ Thank you for using Banking Simulator!");
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < 0) {
                    System.out.println("❌ Amount cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }
}