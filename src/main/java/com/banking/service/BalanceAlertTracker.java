package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Balance Alert Tracker
 * Monitors account balances and sends alerts
 */
public class BalanceAlertTracker {

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

        monitoringTimer = new Timer("BalanceMonitor", true);

        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAllAccountBalances();
            }
        }, 0, MONITORING_INTERVAL);

        isMonitoring = true;
        System.out.println("✓ Balance monitoring started (checking every " +
                (MONITORING_INTERVAL / 1000 / 60) + " minutes)");
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
     * Check all account balances
     */
    public void checkAllAccountBalances() {
        try {
            List<Account> accounts = accountManager.getAllAccounts();
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
        List<Account> allAccounts = accountManager.getAllAccounts();
        return allAccounts.stream()
                .filter(acc -> acc.getBalance() < LOW_BALANCE_THRESHOLD)
                .toList();
    }

    /**
     * Get critical balance accounts
     */
    public List<Account> getCriticalBalanceAccounts() {
        List<Account> allAccounts = accountManager.getAllAccounts();
        return allAccounts.stream()
                .filter(acc -> acc.getBalance() < CRITICAL_BALANCE_THRESHOLD)
                .toList();
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
