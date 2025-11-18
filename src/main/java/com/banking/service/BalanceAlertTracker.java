package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Balance Alert Tracker
 * Monitors account balances and sends alerts
 */
public class BalanceAlertTracker {

    private static final Logger logger = LoggerFactory.getLogger(BalanceAlertTracker.class);

    private AccountManager accountManager;
    private EmailService emailService;
    private static BalanceAlertTracker instance;

    // Alert thresholds
    private static final double LOW_BALANCE_THRESHOLD = 1000.00;
    private static final double CRITICAL_BALANCE_THRESHOLD = 500.00;

    // Monitoring interval (in milliseconds) - default: 1 hour
    private static final long MONITORING_INTERVAL = 60 * 60 * 1000;

    private Timer monitoringTimer;
    private boolean isMonitoring = false;

    private BalanceAlertTracker() {
        this.accountManager = AccountManager.getInstance();
        this.emailService = EmailService.getInstance();
    }

    public static BalanceAlertTracker getInstance() {
        if (instance == null) {
            instance = new BalanceAlertTracker();
        }
        return instance;
    }

    /**
     * Start monitoring account balances
     */
    public void startMonitoring() {
        if (isMonitoring) {
            System.out.println("⚠️ Monitoring is already running");
            return;
        }

        monitoringTimer = new Timer("BalanceMonitor", true); // daemon thread

        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAllAccountBalancesSilent(); // Use silent version for background
            }
        }, MONITORING_INTERVAL, MONITORING_INTERVAL); // Start after 1 hour, not immediately

        isMonitoring = true;
        System.out.println("✓ Balance monitoring started (checking every " +
                (MONITORING_INTERVAL / 1000 / 60) + " minutes)");
        logger.info("Balance monitoring started");
    }

    /**
     * Stop monitoring
     */
    public void stopMonitoring() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            isMonitoring = false;
            System.out.println("✓ Balance monitoring stopped");
        }
    }

    /**
     * Check all account balances - WITH console output
     */
    public void checkAllAccountBalances() {
        try {
            // Use internal method that doesn't require authentication
            // This is for background monitoring service
            List<Account> accounts = accountManager.getAllAccountsInternal();
            int lowBalanceCount = 0;
            int criticalBalanceCount = 0;

            System.out.println("\n[ALERT TRACKER] Checking " + accounts.size() + " accounts...");

            for (Account account : accounts) {
                double balance = account.getBalance();

                if (balance < CRITICAL_BALANCE_THRESHOLD) {
                    // Critical balance - immediate alert
                    sendCriticalBalanceAlert(account);
                    criticalBalanceCount++;
                } else if (balance < LOW_BALANCE_THRESHOLD) {
                    // Low balance warning
                    sendLowBalanceAlert(account);
                    lowBalanceCount++;
                }
            }

            System.out.println("[ALERT TRACKER] Scan complete:");
            System.out.println("  - Low balance alerts: " + lowBalanceCount);
            System.out.println("  - Critical balance alerts: " + criticalBalanceCount);

        } catch (Exception e) {
            System.err.println("❌ Error in balance monitoring: " + e.getMessage());
            logger.error("Error checking account balances", e);
        }
    }

    /**
     * Check all account balances - SILENT (for background monitoring)
     * No console output, only logs to file
     */
    private void checkAllAccountBalancesSilent() {
        try {
            List<Account> accounts = accountManager.getAllAccountsInternal();
            int lowBalanceCount = 0;
            int criticalBalanceCount = 0;

            logger.info("Background monitoring: Checking {} accounts", accounts.size());

            for (Account account : accounts) {
                double balance = account.getBalance();

                if (balance < CRITICAL_BALANCE_THRESHOLD) {
                    sendCriticalBalanceAlertSilent(account);
                    criticalBalanceCount++;
                } else if (balance < LOW_BALANCE_THRESHOLD) {
                    sendLowBalanceAlertSilent(account);
                    lowBalanceCount++;
                }
            }

            logger.info("Background monitoring complete: Low={}, Critical={}",
                    lowBalanceCount, criticalBalanceCount);

        } catch (Exception e) {
            logger.error("Error in background balance monitoring", e);
        }
    }

    /**
     * Check specific account balance
     */
    public void checkAccountBalance(String accountId) {
        try {
            Account account = accountManager.getAccount(accountId);
            double balance = account.getBalance();

            System.out.println("\n[ALERT CHECK] Account: " + accountId);
            System.out.println("Current Balance: ₹" + String.format("%.2f", balance));

            if (balance < CRITICAL_BALANCE_THRESHOLD) {
                System.out.println("⚠️ CRITICAL: Balance below ₹" + CRITICAL_BALANCE_THRESHOLD);
                sendCriticalBalanceAlert(account);
            } else if (balance < LOW_BALANCE_THRESHOLD) {
                System.out.println("⚠️ WARNING: Balance below ₹" + LOW_BALANCE_THRESHOLD);
                sendLowBalanceAlert(account);
            } else {
                System.out.println("✓ Balance is healthy");
            }

        } catch (Exception e) {
            System.err.println("❌ Error checking account: " + e.getMessage());
        }
    }

    /**
     * Send low balance alert
     */
    private void sendLowBalanceAlert(Account account) {
        System.out.println("⚠️ LOW BALANCE ALERT for " + account.getAccountId() +
                " (₹" + String.format("%.2f", account.getBalance()) + ")");

        // Send email alert
        emailService.sendLowBalanceAlert(account, LOW_BALANCE_THRESHOLD);

        // Log alert (you can also save to database)
        logAlert(account.getAccountId(), "LOW_BALANCE", account.getBalance());
    }

    /**
     * Send low balance alert - SILENT (no console output)
     */
    private void sendLowBalanceAlertSilent(Account account) {
        // Send email alert
        emailService.sendLowBalanceAlert(account, LOW_BALANCE_THRESHOLD);

        // Log alert
        logAlert(account.getAccountId(), "LOW_BALANCE", account.getBalance());
        logger.warn("Low balance alert sent for account: {} (Balance: {})",
                account.getAccountId(), account.getBalance());
    }

    /**
     * Send critical balance alert
     */
    private void sendCriticalBalanceAlert(Account account) {
        System.out.println("🚨 CRITICAL BALANCE ALERT for " + account.getAccountId() +
                " (₹" + String.format("%.2f", account.getBalance()) + ")");

        // Send email alert
        emailService.sendLowBalanceAlert(account, CRITICAL_BALANCE_THRESHOLD);

        // Log alert
        logAlert(account.getAccountId(), "CRITICAL_BALANCE", account.getBalance());
    }

    /**
     * Send critical balance alert - SILENT (no console output)
     */
    private void sendCriticalBalanceAlertSilent(Account account) {
        // Send email alert
        emailService.sendLowBalanceAlert(account, CRITICAL_BALANCE_THRESHOLD);

        // Log alert
        logAlert(account.getAccountId(), "CRITICAL_BALANCE", account.getBalance());
        logger.error("Critical balance alert sent for account: {} (Balance: {})",
                account.getAccountId(), account.getBalance());
    }

    /**
     * Log alert to console (can be extended to save to database)
     */
    private void logAlert(String accountId, String alertType, double balance) {
        System.out.println("[ALERT LOG] " +
                java.time.LocalDateTime.now() + " | " +
                "Account: " + accountId + " | " +
                "Type: " + alertType + " | " +
                "Balance: ₹" + String.format("%.2f", balance));
    }

    /**
     * Get low balance accounts
     */
    public List<Account> getLowBalanceAccounts() {
        try {
            // Use internal method for system service
            List<Account> allAccounts = accountManager.getAllAccountsInternal();
            return allAccounts.stream()
                    .filter(acc -> acc.getBalance() < LOW_BALANCE_THRESHOLD)
                    .toList();
        } catch (Exception e) {
            logger.error("Error getting low balance accounts", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get critical balance accounts
     */
    public List<Account> getCriticalBalanceAccounts() {
        try {
            // Use internal method for system service
            List<Account> allAccounts = accountManager.getAllAccountsInternal();
            return allAccounts.stream()
                    .filter(acc -> acc.getBalance() < CRITICAL_BALANCE_THRESHOLD)
                    .toList();
        } catch (Exception e) {
            logger.error("Error getting critical balance accounts", e);
            return new ArrayList<>();
        }
    }

    /**
     * Display alert statistics
     */
    public void displayAlertStatistics() {
        List<Account> lowBalance = getLowBalanceAccounts();
        List<Account> criticalBalance = getCriticalBalanceAccounts();

        System.out.println("\n========== ALERT STATISTICS ==========");
        System.out.println("Low Balance Accounts (<₹" + LOW_BALANCE_THRESHOLD + "): " + lowBalance.size());
        System.out.println("Critical Balance Accounts (<₹" + CRITICAL_BALANCE_THRESHOLD + "): " + criticalBalance.size());
        System.out.println("Monitoring Status: " + (isMonitoring ? "ACTIVE" : "INACTIVE"));
        System.out.println("======================================\n");

        if (!lowBalance.isEmpty()) {
            System.out.println("Low Balance Accounts:");
            for (Account acc : lowBalance) {
                System.out.printf("  %s - %s: ₹%.2f\n",
                        acc.getAccountId(), acc.getHolderName(), acc.getBalance());
            }
        }

        if (!criticalBalance.isEmpty()) {
            System.out.println("\n🚨 Critical Balance Accounts:");
            for (Account acc : criticalBalance) {
                System.out.printf("  %s - %s: ₹%.2f\n",
                        acc.getAccountId(), acc.getHolderName(), acc.getBalance());
            }
        }
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public static double getLowBalanceThreshold() {
        return LOW_BALANCE_THRESHOLD;
    }

    public static double getCriticalBalanceThreshold() {
        return CRITICAL_BALANCE_THRESHOLD;
    }
}