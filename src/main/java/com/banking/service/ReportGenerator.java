package main.java.com.banking.service;

import main.java.com.banking.model.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report Generator
 * Generates and exports reports to text files
 */
public class ReportGenerator {

    private static final String REPORTS_FOLDER = "reports/";
    private AccountManager accountManager;
    private TransactionManager transactionManager;

    public ReportGenerator() {
        this.accountManager = AccountManager.getInstance();
        this.transactionManager = TransactionManager.getInstance();

        // Create reports folder if it doesn't exist
        File folder = new File(REPORTS_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Generate Account Summary Report
     */
    public void generateAccountSummaryReport() throws Exception {
        String filename = REPORTS_FOLDER + "account_summary_" + getTimestamp() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("========================================\n");
            writer.write("       ACCOUNT SUMMARY REPORT\n");
            writer.write("========================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("========================================\n\n");

            List<Account> accounts = accountManager.getAllAccounts();

            writer.write("Total Accounts: " + accounts.size() + "\n\n");

            double totalBalance = 0;

            for (Account account : accounts) {
                writer.write("----------------------------------------\n");
                writer.write("Account ID      : " + account.getAccountId() + "\n");
                writer.write("Holder Name     : " + account.getHolderName() + "\n");
                writer.write("Account Type    : " + account.getAccountType().getDisplayName() + "\n");
                writer.write("Balance         : ₹" + String.format("%.2f", account.getBalance()) + "\n");
                writer.write("Email           : " + account.getEmail() + "\n");
                writer.write("Phone           : " + account.getPhone() + "\n");
                writer.write("Status          : " + account.getStatus() + "\n");
                writer.write("Created Date    : " + account.getCreatedDate().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
                writer.write("----------------------------------------\n\n");

                totalBalance += account.getBalance();
            }

            writer.write("\n========================================\n");
            writer.write("SUMMARY\n");
            writer.write("========================================\n");
            writer.write("Total Accounts  : " + accounts.size() + "\n");
            writer.write("Total Balance   : ₹" + String.format("%.2f", totalBalance) + "\n");
            writer.write("Average Balance : ₹" + String.format("%.2f",
                    accounts.isEmpty() ? 0 : totalBalance / accounts.size()) + "\n");
            writer.write("========================================\n");
        }

        System.out.println("✓ Account summary report generated: " + filename);
    }

    /**
     * Generate Transaction History Report for specific account
     */
    public void generateTransactionReport(String accountId) throws Exception {
        String filename = REPORTS_FOLDER + "transaction_" + accountId + "_" + getTimestamp() + ".txt";

        Account account = accountManager.getAccount(accountId);
        List<Transaction> transactions = transactionManager.getTransactionHistory(accountId);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("========================================\n");
            writer.write("     TRANSACTION HISTORY REPORT\n");
            writer.write("========================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("========================================\n\n");

            writer.write("Account Details:\n");
            writer.write("----------------------------------------\n");
            writer.write("Account ID      : " + account.getAccountId() + "\n");
            writer.write("Holder Name     : " + account.getHolderName() + "\n");
            writer.write("Current Balance : ₹" + String.format("%.2f", account.getBalance()) + "\n");
            writer.write("Account Type    : " + account.getAccountType().getDisplayName() + "\n");
            writer.write("----------------------------------------\n\n");

            writer.write("Transaction History:\n");
            writer.write("Total Transactions: " + transactions.size() + "\n\n");

            if (transactions.isEmpty()) {
                writer.write("No transactions found.\n");
            } else {
                writer.write(String.format("%-8s %-15s %-12s %-15s %-20s %s\n",
                        "TXN ID", "TYPE", "AMOUNT", "BALANCE", "DATE", "DESCRIPTION"));
                writer.write("=".repeat(100) + "\n");

                for (Transaction t : transactions) {
                    writer.write(String.format("%-8d %-15s ₹%-11.2f ₹%-14.2f %-20s %s\n",
                            t.getTransactionId(),
                            t.getTransactionType().getDisplayName(),
                            t.getAmount(),
                            t.getBalanceAfter(),
                            t.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                            t.getDescription()
                    ));
                }
            }

            writer.write("\n========================================\n");
        }

        System.out.println("✓ Transaction report generated: " + filename);
    }

    /**
     * Generate All Transactions Report
     */
    public void generateAllTransactionsReport() throws Exception {
        String filename = REPORTS_FOLDER + "all_transactions_" + getTimestamp() + ".txt";

        List<Transaction> transactions = transactionManager.getAllTransactions();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("========================================\n");
            writer.write("    ALL TRANSACTIONS REPORT\n");
            writer.write("========================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("Total Transactions: " + transactions.size() + "\n");
            writer.write("========================================\n\n");

            if (transactions.isEmpty()) {
                writer.write("No transactions found.\n");
            } else {
                writer.write(String.format("%-8s %-12s %-15s %-12s %-15s %-20s %s\n",
                        "TXN ID", "ACCOUNT", "TYPE", "AMOUNT", "BALANCE", "DATE", "DESCRIPTION"));
                writer.write("=".repeat(120) + "\n");

                for (Transaction t : transactions) {
                    writer.write(String.format("%-8d %-12s %-15s ₹%-11.2f ₹%-14.2f %-20s %s\n",
                            t.getTransactionId(),
                            t.getAccountId(),
                            t.getTransactionType().getDisplayName(),
                            t.getAmount(),
                            t.getBalanceAfter(),
                            t.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                            t.getDescription()
                    ));
                }
            }

            writer.write("\n========================================\n");
        }

        System.out.println("✓ All transactions report generated: " + filename);
    }

    /**
     * Generate Low Balance Alert Report
     */
    public void generateLowBalanceReport(double threshold) throws Exception {
        String filename = REPORTS_FOLDER + "low_balance_alert_" + getTimestamp() + ".txt";

        List<Account> allAccounts = accountManager.getAllAccounts();
        List<Account> lowBalanceAccounts = allAccounts.stream()
                .filter(acc -> acc.getBalance() < threshold)
                .toList();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("========================================\n");
            writer.write("     LOW BALANCE ALERT REPORT\n");
            writer.write("========================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("Threshold: ₹" + String.format("%.2f", threshold) + "\n");
            writer.write("========================================\n\n");

            writer.write("Accounts Below Threshold: " + lowBalanceAccounts.size() + "\n\n");

            if (lowBalanceAccounts.isEmpty()) {
                writer.write("No accounts found with balance below threshold.\n");
            } else {
                for (Account account : lowBalanceAccounts) {
                    writer.write("----------------------------------------\n");
                    writer.write("Account ID      : " + account.getAccountId() + "\n");
                    writer.write("Holder Name     : " + account.getHolderName() + "\n");
                    writer.write("Current Balance : ₹" + String.format("%.2f", account.getBalance()) + "\n");
                    writer.write("Email           : " + account.getEmail() + "\n");
                    writer.write("Phone           : " + account.getPhone() + "\n");
                    writer.write("⚠️ WARNING: Balance below minimum threshold!\n");
                    writer.write("----------------------------------------\n\n");
                }
            }

            writer.write("\n========================================\n");
        }

        System.out.println("✓ Low balance report generated: " + filename);
    }

    /**
     * Export Account Data to CSV
     */
    public void exportAccountsToCSV() throws Exception {
        String filename = REPORTS_FOLDER + "accounts_export_" + getTimestamp() + ".csv";

        List<Account> accounts = accountManager.getAllAccounts();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write CSV header
            writer.write("Account_ID,Holder_Name,Balance,Account_Type,Email,Phone,Status,Created_Date\n");

            // Write data
            for (Account account : accounts) {
                writer.write(String.format("%s,%s,%.2f,%s,%s,%s,%s,%s\n",
                        account.getAccountId(),
                        account.getHolderName(),
                        account.getBalance(),
                        account.getAccountType().name(),
                        account.getEmail(),
                        account.getPhone(),
                        account.getStatus(),
                        account.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                ));
            }
        }

        System.out.println("✓ Accounts exported to CSV: " + filename);
    }

    /**
     * Get timestamp for filename
     */
    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}