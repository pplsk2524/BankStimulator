package main.java.com.banking.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Manager - Simple Version
 * Loads configuration from external properties file
 * Keeps sensitive credentials out of source code
 */
public class ConfigManager {

    private static Properties properties;
    private static boolean loaded = false;

    /**
     * Load configuration from file
     */
    private static void loadConfiguration() {
        if (loaded) return;

        properties = new Properties();

        // Try multiple locations
        String[] configPaths = {
                "resources/config.properties",           // From project root
                "config.properties",                     // From current directory
                "../resources/config.properties",        // From target directory
                "src/main/resources/config.properties"   // Maven standard location
        };

        boolean configLoaded = false;

        for (String path : configPaths) {
            try (InputStream input = new FileInputStream(path)) {
                properties.load(input);
                System.out.println("✓ Configuration loaded from: " + path);
                configLoaded = true;
                loaded = true;
                return;
            } catch (IOException e) {
                // Try next path
            }
        }

        // Try loading from classpath as fallback
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("✓ Configuration loaded from classpath");
                configLoaded = true;
                loaded = true;
                return;
            }
        } catch (IOException e) {
            // Continue to defaults
        }

        if (!configLoaded) {
            System.err.println("⚠️  Warning: config.properties not found!");
            System.err.println("⚠️  Using default configuration (may not work)");
            System.err.println("⚠️  Please create resources/config.properties file");
            loadDefaultConfiguration();
        }
    }

    /**
     * Load default configuration if file not found
     */
    private static void loadDefaultConfiguration() {
        properties = new Properties();
        // Database defaults
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/banking_simulator");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "password");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        // Email defaults
        properties.setProperty("email.smtp.host", "smtp.gmail.com");
        properties.setProperty("email.smtp.port", "587");
        properties.setProperty("email.from", "");
        properties.setProperty("email.password", "");
        properties.setProperty("email.enabled", "false");

        // System defaults
        properties.setProperty("system.minimum.balance", "500.00");
        properties.setProperty("system.low.balance.threshold", "1000.00");
        properties.setProperty("system.critical.balance.threshold", "500.00");

        loaded = true;
    }

    /**
     * Get property value
     */
    private static String getProperty(String key, String defaultValue) {
        if (!loaded) {
            loadConfiguration();
        }
        return properties.getProperty(key, defaultValue);
    }

    // ==================== DATABASE CONFIGURATION ====================

    public static String getDatabaseUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/banking_simulator");
    }

    public static String getDatabaseUsername() {
        return getProperty("db.username", "root");
    }

    public static String getDatabasePassword() {
        return getProperty("db.password", "password");
    }

    public static String getDatabaseDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    // ==================== EMAIL CONFIGURATION ====================

    public static String getEmailHost() {
        return getProperty("email.smtp.host", "smtp.gmail.com");
    }

    public static String getEmailPort() {
        return getProperty("email.smtp.port", "587");
    }

    public static String getEmailFrom() {
        return getProperty("email.from", "");
    }

    public static String getEmailPassword() {
        return getProperty("email.password", "");
    }

    public static boolean isEmailEnabled() {
        return Boolean.parseBoolean(getProperty("email.enabled", "false"));
    }

    // ==================== SYSTEM CONFIGURATION ====================

    public static double getMinimumBalance() {
        return Double.parseDouble(getProperty("system.minimum.balance", "500.00"));
    }

    public static double getLowBalanceThreshold() {
        return Double.parseDouble(getProperty("system.low.balance.threshold", "1000.00"));
    }

    public static double getCriticalBalanceThreshold() {
        return Double.parseDouble(getProperty("system.critical.balance.threshold", "500.00"));
    }

    public static long getMonitoringInterval() {
        return Long.parseLong(getProperty("system.monitoring.interval", "3600000"));
    }

    /**
     * Reload configuration (useful for testing)
     */
    public static void reloadConfiguration() {
        loaded = false;
        loadConfiguration();
    }

    /**
     * Display current configuration (hide passwords)
     */
    public static void displayConfiguration() {
        System.out.println("\n========== CURRENT CONFIGURATION ==========");
        System.out.println("Database URL: " + getDatabaseUrl());
        System.out.println("Database User: " + getDatabaseUsername());
        System.out.println("Database Password: " + maskPassword(getDatabasePassword()));
        System.out.println("Email Enabled: " + isEmailEnabled());
        if (isEmailEnabled()) {
            System.out.println("Email From: " + getEmailFrom());
            System.out.println("Email Password: " + maskPassword(getEmailPassword()));
        }
        System.out.println("Minimum Balance: ₹" + getMinimumBalance());
        System.out.println("Low Balance Threshold: ₹" + getLowBalanceThreshold());
        System.out.println("===========================================\n");
    }

    /**
     * Mask password for display
     */
    private static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "(not set)";
        }
        return "****" + (password.length() > 4 ? password.substring(password.length() - 4) : "");
    }
}
