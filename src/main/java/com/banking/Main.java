package main.java.com.banking;

import main.java.com.banking.model.*;
import main.java.com.banking.service.*;
import main.java.com.banking.util.DatabaseConfig;

import java.util.Scanner;

/**
 * Main Application - Banking Transaction Simulator
 * Milestone 3 & 4 - Complete Implementation
 */
public class Main {

    private static AccountManager accountManager;
    private static TransactionManager transactionManager;
    private static ReportGenerator reportGenerator;
    private static EmailService emailService;
    private static BalanceAlertTracker alertTracker;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Initialize
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  Banking Transaction Simulator v2.0    ║");
        System.out.println("║                                        ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        // Test database connection
        if (!DatabaseConfig.testConnection()) {
            System.err.println("❌ Failed to connect to database. Please check your configuration.");
            return;
        }

        // Initialize all services
        accountManager = AccountManager.getInstance();
        transactionManager = TransactionManager.getInstance();
        reportGenerator = new ReportGenerator();
        emailService = EmailService.getInstance();
        alertTracker = BalanceAlertTracker.getInstance();

        // Start balance monitoring
        alertTracker.startMonitoring();

        // Main menu loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    accountManagementMenu();
                    break;
                case 2:
                    transactionMenu();
                    break;
                case 3:
                    reportsMenu();
                    break;
                case 4:
                    alertsMenu();
                    break;
                case 5:
                    displayStatistics();
                    break;
                case 6:
                    running = false;
                    cleanup();
                    System.out.println("\n✓ Thank you for using Banking Simulator!");
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * Display main menu
     */
    private static void displayMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Account Management");
        System.out.println("2. Transactions");
        System.out.println("3. Reports");
        System.out.println("4. Alerts & Monitoring");
        System.out.println("5. Statistics");
        System.out.println("6. Exit");
        System.out.println("===============================");
    }

    /**
     * Account Management Menu
     */
    private static void accountManagementMenu() {
        while (true) {
            System.out.println("\n--- ACCOUNT MANAGEMENT ---");
            System.out.println("1. Create New Account");
            System.out.println("2. View Account Details");
            System.out.println("3. View All Accounts");
            System.out.println("4. Delete Account");
            System.out.println("5. Back to Main Menu");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1: createAccountMenu(); break;
                case 2: viewAccountMenu(); break;
                case 3: accountManager.displayAllAccounts(); break;
                case 4: deleteAccountMenu(); break;
                case 5: return;
                default: System.out.println("❌ Invalid choice");
            }
        }
    }

    /**
     * Transaction Menu
     */
    private static void transactionMenu() {
        while (true) {
            System.out.println("\n--- TRANSACTIONS ---");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. View Transaction History");
            System.out.println("5. Back to Main Menu");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1: depositMenu(); break;
                case 2: withdrawMenu(); break;
                case 3: transferMenu(); break;
                case 4: viewTransactionHistoryMenu(); break;
                case 5: return;
                default: System.out.println("❌ Invalid choice");
            }
        }
    }

    /**
     * Reports Menu
     */
    private static void reportsMenu() {
        while (true) {
            System.out.println("\n--- REPORTS ---");
            System.out.println("1. Generate Account Summary Report");
            System.out.println("2. Generate Transaction Report");
            System.out.println("3. Generate All Transactions Report");
            System.out.println("4. Generate Low Balance Report");
            System.out.println("5. Export Accounts to CSV");
            System.out.println("6. Back to Main Menu");

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

    /**
     * Alerts Menu
     */
    private static void alertsMenu() {
        while (true) {
            System.out.println("\n--- ALERTS & MONITORING ---");
            System.out.println("1. Check All Account Balances");
            System.out.println("2. Check Specific Account");
            System.out.println("3. View Alert Statistics");
            System.out.println("4. Start/Stop Monitoring");
            System.out.println("5. Test Email Configuration");
            System.out.println("6. Back to Main Menu");

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

    /**
     * Create account menu
     */
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
                case 1: accountType = AccountType.SAVINGS; break;
                case 2: accountType = AccountType.CURRENT; break;
                case 3: accountType = AccountType.FIXED_DEPOSIT; break;
                case 4: accountType = AccountType.SALARY; break;
                default:
                    System.out.println("❌ Invalid type. Defaulting to SAVINGS");
                    accountType = AccountType.SAVINGS;
            }

            System.out.print("Enter Email (e.g., user@example.com): ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Phone (10 digits): ");
            String phone = scanner.nextLine().trim();

            // Create account
            Account account = accountManager.createAccount(
                    accountId, holderName, initialBalance, accountType, email, phone
            );

            System.out.println("\n✓ Account created successfully!");
            System.out.println(account);

            // Send welcome email (optional)
            System.out.print("\nSend welcome email? (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                emailService.sendWelcomeEmail(account);
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Deposit menu
     */
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

    /**
     * Withdraw menu
     */
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

            // Check if balance is low
            alertTracker.checkAccountBalance(accountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    /**
     * Transfer menu
     */
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
            Account toAccount = accountManager.getAccount(toAccountId);

            System.out.println("\nUpdated Balances:");
            System.out.println(fromAccountId + ": ₹" + String.format("%.2f", fromAccount.getBalance()));
            System.out.println(toAccountId + ": ₹" + String.format("%.2f", toAccount.getBalance()));

            // Check if source balance is low
            alertTracker.checkAccountBalance(fromAccountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    /**
     * View transaction history
     */
    private static void viewTransactionHistoryMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

            transactionManager.displayTransactionHistory(accountId);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    /**
     * View account menu
     */
    private static void viewAccountMenu() {
        try {
            System.out.print("\nEnter Account ID: ");
            String accountId = scanner.nextLine().trim();

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

            // Show transaction count
            int txnCount = transactionManager.getTransactionCount(accountId);
            System.out.println("Total Transactions: " + txnCount);

        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    /**
     * Delete account menu
     */
    private static void deleteAccountMenu() {
        try {
            System.out.print("\nEnter Account ID to delete: ");
            String accountId = scanner.nextLine().trim();

            // Confirm deletion
            System.out.print("Are you sure? (yes/no): ");
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

    /**
     * Display statistics
     */
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

            System.out.println("=======================================\n");
        } catch (Exception e) {
            System.err.println("❌ Error displaying statistics: " + e.getMessage());
        }
    }

    /**
     * Toggle monitoring on/off
     */
    private static void toggleMonitoring() {
        if (alertTracker.isMonitoring()) {
            alertTracker.stopMonitoring();
        } else {
            alertTracker.startMonitoring();
        }
    }

    /**
     * Cleanup before exit
     */
    private static void cleanup() {
        alertTracker.stopMonitoring();
        DatabaseConfig.closeConnection();
    }

    /**
     * Utility: Get integer input with validation
     */
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

    /**
     * Utility: Get double input with validation
     */
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