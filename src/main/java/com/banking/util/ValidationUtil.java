package main.java.com.banking.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating user inputs
 */
public class ValidationUtil {

    // Regex patterns for validation
    private static final Pattern ACCOUNT_ID_PATTERN = Pattern.compile("^ACC\\d{3,6}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z\\s]{2,49}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    /**
     * Validate Account ID
     * Format: ACC followed by 3-6 digits (e.g., ACC001, ACC123456)
     */
    public static boolean isValidAccountId(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_ID_PATTERN.matcher(accountId.trim()).matches();
    }

    /**
     * Validate Holder Name
     * Rules:
     * - Must start with a letter
     * - Can contain letters and spaces
     * - Length: 3-50 characters
     * - No special characters or numbers
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmedName = name.trim();
        return NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validate Email
     * Format: standard email format (user@domain.com)
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate Phone Number
     * Format: Indian mobile number (10 digits starting with 6-9)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate Amount
     * Rules:
     * - Must be positive
     * - Must be greater than 0
     * - Maximum 2 decimal places
     */
    public static boolean isValidAmount(double amount) {
        if (amount <= 0) {
            return false;
        }
        // Check for maximum 2 decimal places
        String amountStr = String.valueOf(amount);
        int decimalIndex = amountStr.indexOf('.');
        if (decimalIndex != -1) {
            int decimalPlaces = amountStr.length() - decimalIndex - 1;
            return decimalPlaces <= 2;
        }
        return true;
    }

    /**
     * Validate Initial Balance
     * Rules:
     * - Must be non-negative
     * - Can be zero for some account types
     */
    public static boolean isValidInitialBalance(double balance) {
        return balance >= 0;
    }

    // ==================== ERROR MESSAGE GENERATORS ====================

    public static String getAccountIdError() {
        return "Invalid Account ID! Format: ACC followed by 3-6 digits (e.g., ACC001, ACC123456)";
    }

    public static String getNameError() {
        return "Invalid Name! Must be 3-50 characters, letters and spaces only, start with a letter";
    }

    public static String getEmailError() {
        return "Invalid Email! Format: user@domain.com";
    }

    public static String getPhoneError() {
        return "Invalid Phone! Must be 10 digits starting with 6-9 (Indian mobile number)";
    }

    public static String getAmountError() {
        return "Invalid Amount! Must be positive with maximum 2 decimal places";
    }

    public static String getInitialBalanceError() {
        return "Invalid Initial Balance! Must be non-negative";
    }

    // ==================== SANITIZATION METHODS ====================

    /**
     * Sanitize and format name (trim, proper case)
     */
    public static String sanitizeName(String name) {
        if (name == null) return "";

        String trimmed = name.trim();
        String[] words = trimmed.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Sanitize email (trim, lowercase)
     */
    public static String sanitizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    /**
     * Sanitize phone (remove spaces, dashes)
     */
    public static String sanitizePhone(String phone) {
        if (phone == null) return "";
        return phone.trim().replaceAll("[\\s-]", "");
    }

    /**
     * Sanitize account ID (trim, uppercase)
     */
    public static String sanitizeAccountId(String accountId) {
        if (accountId == null) return "";
        return accountId.trim().toUpperCase();
    }
}
