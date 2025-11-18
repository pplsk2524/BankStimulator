package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import main.java.com.banking.util.ConfigManager;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class EmailService {

    private static EmailService instance;

    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Check if email is enabled in configuration
     */
    private boolean isEmailEnabled() {
        return ConfigManager.isEmailEnabled();
    }

    /**
     * Send low balance alert email
     */
    public void sendLowBalanceAlert(Account account, double threshold) {
        if (!isEmailEnabled()) {
            System.out.println("ℹ️  Email is disabled in config. Skipping email alert.");
            return;
        }

        try {
            String subject = "⚠️ Low Balance Alert - " + account.getAccountId();
            String body = buildLowBalanceEmailBody(account, threshold);
            sendEmail(account.getEmail(), subject, body);
            System.out.println("✓ Low balance alert sent to: " + account.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send transaction confirmation email
     */
    public void sendTransactionAlert(Account account, String transactionType, double amount, double newBalance) {
        if (!isEmailEnabled()) {
            return;
        }

        try {
            String subject = "Transaction Alert - " + account.getAccountId();
            String body = buildTransactionEmailBody(account, transactionType, amount, newBalance);
            sendEmail(account.getEmail(), subject, body);
            System.out.println("✓ Transaction alert sent to: " + account.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send welcome email for new account
     */
    public void sendWelcomeEmail(Account account) {
        if (!isEmailEnabled()) {
            System.out.println("ℹ️  Email is disabled. Welcome email not sent.");
            return;
        }

        try {
            String subject = "Welcome to Banking Simulator - " + account.getAccountId();
            String body = buildWelcomeEmailBody(account);
            sendEmail(account.getEmail(), subject, body);
            System.out.println("✓ Welcome email sent to: " + account.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Core email sending method
     * ✅ All credentials loaded from config.properties
     */
    private void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        // ✅ Get configuration from external config file (NO hardcoded values!)
        String smtpHost = ConfigManager.getEmailHost();
        String smtpPort = ConfigManager.getEmailPort();
        String fromEmail = ConfigManager.getEmailFrom();
        String emailPassword = ConfigManager.getEmailPassword();

        // Validate configuration
        if (fromEmail == null || fromEmail.isEmpty()) {
            throw new MessagingException("Email 'from' address not configured in config.properties");
        }

        if (emailPassword == null || emailPassword.isEmpty()) {
            throw new MessagingException("Email password not configured in config.properties");
        }

        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        // Send message
        Transport.send(message);
    }

    /**
     * Build low balance email body
     */
    private String buildLowBalanceEmailBody(Account account, double threshold) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(account.getHolderName()).append(",\n\n");
        body.append("This is to inform you that your account balance has fallen below the minimum threshold.\n\n");
        body.append("Account Details:\n");
        body.append("================\n");
        body.append("Account ID: ").append(account.getAccountId()).append("\n");
        body.append("Current Balance: ₹").append(String.format("%.2f", account.getBalance())).append("\n");
        body.append("Minimum Threshold: ₹").append(String.format("%.2f", threshold)).append("\n");
        body.append("Account Type: ").append(account.getAccountType().getDisplayName()).append("\n");
        body.append("Date: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");
        body.append("⚠️ WARNING: Please maintain minimum balance to avoid account penalties.\n\n");
        body.append("Please deposit funds at your earliest convenience.\n\n");
        body.append("Thank you,\n");
        body.append("Banking Simulator Team");
        return body.toString();
    }

    /**
     * Build transaction email body
     */
    private String buildTransactionEmailBody(Account account, String transactionType,
                                             double amount, double newBalance) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(account.getHolderName()).append(",\n\n");
        body.append("A transaction has been processed on your account.\n\n");
        body.append("Transaction Details:\n");
        body.append("====================\n");
        body.append("Account ID: ").append(account.getAccountId()).append("\n");
        body.append("Transaction Type: ").append(transactionType).append("\n");
        body.append("Amount: ₹").append(String.format("%.2f", amount)).append("\n");
        body.append("New Balance: ₹").append(String.format("%.2f", newBalance)).append("\n");
        body.append("Date & Time: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");
        body.append("If you did not authorize this transaction, please contact us immediately.\n\n");
        body.append("Thank you,\n");
        body.append("Banking Simulator Team");
        return body.toString();
    }

    /**
     * Build welcome email body
     */
    private String buildWelcomeEmailBody(Account account) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(account.getHolderName()).append(",\n\n");
        body.append("Welcome to Banking Simulator!\n\n");
        body.append("Your account has been successfully created.\n\n");
        body.append("Account Details:\n");
        body.append("================\n");
        body.append("Account ID: ").append(account.getAccountId()).append("\n");
        body.append("Account Type: ").append(account.getAccountType().getDisplayName()).append("\n");
        body.append("Initial Balance: ₹").append(String.format("%.2f", account.getBalance())).append("\n");
        body.append("Email: ").append(account.getEmail()).append("\n");
        body.append("Phone: ").append(account.getPhone()).append("\n");
        body.append("Created: ").append(account.getCreatedDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");
        body.append("You can now perform transactions on your account.\n\n");
        body.append("Thank you for choosing Banking Simulator!\n\n");
        body.append("Best Regards,\n");
        body.append("Banking Simulator Team");
        return body.toString();
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration() {
        if (!isEmailEnabled()) {
            System.out.println("\nℹ️  Email is disabled in config.properties");
            System.out.println("   Set email.enabled=true to enable email functionality");
            return false;
        }

        try {
            System.out.println("\nTesting email configuration...");
            String fromEmail = ConfigManager.getEmailFrom();

            if (fromEmail == null || fromEmail.isEmpty()) {
                System.err.println("❌ Email not configured in config.properties");
                printEmailSetupInstructions();
                return false;
            }

            // Send test email to self
            sendEmail(fromEmail, "Test Email - Banking Simulator",
                    "This is a test email from Banking Simulator.\n\nIf you received this, email configuration is working correctly!");

            System.out.println("✓ Email configuration test successful!");
            System.out.println("✓ Test email sent to: " + fromEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed!");
            System.err.println("Error: " + e.getMessage());
            printEmailSetupInstructions();
            return false;
        }
    }

    /**
     * Print email setup instructions
     */
    private void printEmailSetupInstructions() {
        System.err.println("\n========== EMAIL SETUP INSTRUCTIONS ==========");
        System.err.println("1. Open: resources/config.properties");
        System.err.println("2. Set: email.enabled=true");
        System.err.println("3. Set: email.from=your_email@gmail.com");
        System.err.println("4. Enable 2FA in Gmail:");
        System.err.println("   → https://myaccount.google.com/security");
        System.err.println("5. Generate App Password:");
        System.err.println("   → https://myaccount.google.com/apppasswords");
        System.err.println("   → Select: Mail + Other (Banking Simulator)");
        System.err.println("6. Set: email.password=GENERATED_APP_PASSWORD");
        System.err.println("7. Save config.properties");
        System.err.println("\n⚠️  IMPORTANT: Never commit config.properties to Git!");
        System.err.println("==============================================\n");
    }
}