package main.java.com.banking.service;

import main.java.com.banking.model.Account;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Email Service
 * Sends email alerts for low balance and transactions
 *
 * NOTE: You need to add mail.jar and activation.jar to your lib folder
 * Download from: https://javaee.github.io/javamail/
 */
public class EmailService {

    // Email configuration - UPDATE THESE WITH YOUR CREDENTIALS
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "your_email@gmail.com"; // Change this
    private static final String EMAIL_PASSWORD = "your_app_password"; // Use App Password, not regular password

    private static EmailService instance;

    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Send low balance alert email
     */
    public void sendLowBalanceAlert(Account account, double threshold) {
        try {
            String subject = "⚠️ Low Balance Alert - " + account.getAccountId();

            String body = buildLowBalanceEmailBody(account, threshold);

            sendEmail(account.getEmail(), subject, body);

            System.out.println("✓ Low balance alert sent to: " + account.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            // Don't throw exception - email failure shouldn't stop transaction
        }
    }

    /**
     * Send transaction confirmation email
     */
    public void sendTransactionAlert(Account account, String transactionType, double amount, double newBalance) {
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
     */
    private void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
            }
        });

        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
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
        try {
            System.out.println("Testing email configuration...");
            sendEmail(FROM_EMAIL, "Test Email", "This is a test email from Banking Simulator.");
            System.out.println("✓ Email configuration test successful!");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            System.err.println("\nTo fix email issues:");
            System.err.println("1. Enable 2-factor authentication in Gmail");
            System.err.println("2. Generate App Password: https://myaccount.google.com/apppasswords");
            System.err.println("3. Update FROM_EMAIL and EMAIL_PASSWORD in EmailService.java");
            return false;
        }
    }
}