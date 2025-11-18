package main.java.com.banking.util;

import java.util.Scanner;

/**
 * Password Hash Generator Utility
 * Use this to generate BCrypt hashes for inserting into database
 *
 * Run this class to generate hashes for your users
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  Password Hash Generator (BCrypt)     ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        while (true) {
            System.out.print("Enter password to hash (or 'exit' to quit): ");
            String password = scanner.nextLine().trim();

            if (password.equalsIgnoreCase("exit")) {
                break;
            }

            if (password.isEmpty()) {
                System.out.println("❌ Password cannot be empty!\n");
                continue;
            }

            // Check password strength
            if (!SecurityUtil.isStrongPassword(password)) {
                System.out.println("⚠️  Weak password! " + SecurityUtil.getPasswordStrengthMessage());
                System.out.print("Continue anyway? (yes/no): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("yes")) {
                    continue;
                }
            }

            try {
                // Generate hash
                String hash = SecurityUtil.hashPassword(password);

                System.out.println("\n✓ Password hashed successfully!");
                System.out.println("═══════════════════════════════════════");
                System.out.println("Plain Password : " + password);
                System.out.println("BCrypt Hash    : " + hash);
                System.out.println("═══════════════════════════════════════");
                System.out.println("\nCopy this hash to insert into database:");
                System.out.println("INSERT INTO users (..., password_hash, ...) VALUES (..., '" + hash + "', ...);\n");

                // Verify the hash works
                boolean verified = SecurityUtil.verifyPassword(password, hash);
                System.out.println("Verification test: " + (verified ? "✓ PASS" : "❌ FAIL") + "\n");

            } catch (Exception e) {
                System.err.println("❌ Error generating hash: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("\nGoodbye!");
    }

    /**
     * Quick method to generate common hashes
     */
    public static void generateDefaultHashes() {
        System.out.println("Generating default password hashes...\n");

        String[][] defaults = {
                {"Admin@123", "Default admin password"},
                {"Employee@123", "Default employee password"},
                {"Customer@123", "Default customer password"}
        };

        for (String[] entry : defaults) {
            String password = entry[0];
            String description = entry[1];
            String hash = SecurityUtil.hashPassword(password);

            System.out.println(description + ":");
            System.out.println("  Password: " + password);
            System.out.println("  Hash: " + hash);
            System.out.println();
        }
    }
}