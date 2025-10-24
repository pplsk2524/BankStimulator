package main.java.com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Configuration and Connection Management
 * Now uses external config.properties file for credentials
 */
public class DatabaseConfig {

    private static Connection connection = null;

    /**
     * Get database connection using external configuration
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Load credentials from external config
                String url = ConfigManager.getDatabaseUrl();
                String username = ConfigManager.getDatabaseUsername();
                String password = ConfigManager.getDatabasePassword();
                String driver = ConfigManager.getDatabaseDriver();

                // Load MySQL JDBC Driver
                Class.forName(driver);

                // Establish connection
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("✓ Database connection established successfully!");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found! Check Maven dependencies.", e);
        } catch (SQLException e) {
            System.err.println("\n❌ Database Connection Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database 'banking_simulator' exists");
            System.err.println("3. Credentials in resources/config.properties are correct");
            System.err.println("4. config.properties file exists");
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn != null && !conn.isClosed();
            if (isValid) {
                System.out.println("✓ Database connection test: SUCCESS");
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("❌ Database connection test: FAILED");
            return false;
        }
    }
}