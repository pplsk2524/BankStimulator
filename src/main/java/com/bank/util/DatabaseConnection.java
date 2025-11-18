package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility Class
 * Manages MySQL database connections for the Bank Simulation System
 */
public class DatabaseConnection {

    // Database credentials - UPDATE THESE WITH YOUR VALUES
    private static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "poojitha";  // ← CHANGE THIS!

    // Optional: Connection pool settings
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver (optional in newer versions but good practice)
            Class.forName(DRIVER);

            // Establish and return connection
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            throw new SQLException("Driver not found", e);
        }
    }

    /**
     * Test the database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database connection successful!");
            System.out.println("  Connected to: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("  Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close a database connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Main method for testing connection
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...\n");
        testConnection();
    }
}