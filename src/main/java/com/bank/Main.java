package com.bank;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===== BANK SIMULATION SYSTEM =====");
            System.out.println("1. Customer Operations");
            System.out.println("2. Account Operations");
            System.out.println("3. Transaction Operations");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> customerMenu();
                case 2 -> accountMenu();
                case 3 -> transactionMenu();
                case 4 -> {
                    System.out.println("Exiting... Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option! Try again.");
            }
        }
    }

    // ================= CUSTOMER MENU =================
    private static void customerMenu() {
        System.out.println("\n--- CUSTOMER OPERATIONS ---");
        System.out.println("1. Add Customer");
        System.out.println("2. View Customer by ID");
        System.out.println("3. View Customer by Email");
        System.out.println("4. Update Customer");
        System.out.println("5. Delete Customer");
        System.out.println("6. List All Customers");
        System.out.print("Choose option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> addCustomer();
            case 2 -> viewCustomerById();
            case 3 -> viewCustomerByEmail();
            case 4 -> updateCustomer();
            case 5 -> deleteCustomer();
            case 6 -> listAllCustomers();
            default -> System.out.println("Invalid option!");
        }
    }

    private static void addCustomer() {
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Date of Birth (yyyy-mm-dd): ");
        LocalDate dob = LocalDate.parse(scanner.nextLine());

        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setDateOfBirth(dob);

        customerDAO.addCustomer(customer);
    }

    private static void viewCustomerById() {
        System.out.print("Customer ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Customer customer = customerDAO.getCustomerById(id);
        if (customer != null) System.out.println(customer);
        else System.out.println("Customer not found!");
    }

    private static void viewCustomerByEmail() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        Customer customer = customerDAO.getCustomerByEmail(email);
        if (customer != null) System.out.println(customer);
        else System.out.println("Customer not found!");
    }

    private static void updateCustomer() {
        System.out.print("Customer ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Customer customer = customerDAO.getCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }

        System.out.print("First Name (" + customer.getFirstName() + "): ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name (" + customer.getLastName() + "): ");
        String lastName = scanner.nextLine();
        System.out.print("Email (" + customer.getEmail() + "): ");
        String email = scanner.nextLine();
        System.out.print("Phone (" + customer.getPhone() + "): ");
        String phone = scanner.nextLine();
        System.out.print("Address (" + customer.getAddress() + "): ");
        String address = scanner.nextLine();
        System.out.print("Date of Birth (" + customer.getDateOfBirth() + ") yyyy-mm-dd: ");
        String dobInput = scanner.nextLine();

        if (!firstName.isEmpty()) customer.setFirstName(firstName);
        if (!lastName.isEmpty()) customer.setLastName(lastName);
        if (!email.isEmpty()) customer.setEmail(email);
        if (!phone.isEmpty()) customer.setPhone(phone);
        if (!address.isEmpty()) customer.setAddress(address);
        if (!dobInput.isEmpty()) customer.setDateOfBirth(LocalDate.parse(dobInput));

        customerDAO.updateCustomer(customer);
    }

    private static void deleteCustomer() {
        System.out.print("Customer ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        customerDAO.deleteCustomer(id);
    }

    private static void listAllCustomers() {
        List<Customer> customers = customerDAO.getAllCustomers();
        customers.forEach(System.out::println);
    }

    // ================= ACCOUNT MENU =================
    private static void accountMenu() {
        System.out.println("\n--- ACCOUNT OPERATIONS ---");
        System.out.println("1. Create Account");
        System.out.println("2. View Account by ID");
        System.out.println("3. View Account by Number");
        System.out.println("4. View Customer Accounts");
        System.out.println("5. Deposit Money");
        System.out.println("6. Withdraw Money");
        System.out.println("7. Update Account Status");
        System.out.println("8. Delete Account");
        System.out.print("Choose option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> createAccount();
            case 2 -> viewAccountById();
            case 3 -> viewAccountByNumber();
            case 4 -> viewCustomerAccounts();
            case 5 -> depositMoney();
            case 6 -> withdrawMoney();
            case 7 -> updateAccountStatus();
            case 8 -> deleteAccount();
            default -> System.out.println("Invalid option!");
        }
    }

    private static void createAccount() {
        System.out.print("Customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Account Type (SAVINGS/CURRENT/FIXED_DEPOSIT): ");
        String typeStr = scanner.nextLine();

        Account.AccountType type;
        try {
            type = Account.AccountType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid account type!");
            return;
        }

        Account account = new Account(customerId, accountNumber, type);
        accountDAO.createAccount(account);
    }

    private static void viewAccountById() {
        System.out.print("Account ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Account account = accountDAO.getAccountById(id);
        if (account != null) System.out.println(account);
        else System.out.println("Account not found!");
    }

    private static void viewAccountByNumber() {
        System.out.print("Account Number: ");
        String number = scanner.nextLine();
        Account account = accountDAO.getAccountByNumber(number);
        if (account != null) System.out.println(account);
        else System.out.println("Account not found!");
    }

    private static void viewCustomerAccounts() {
        System.out.print("Customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine();
        List<Account> accounts = accountDAO.getAccountsByCustomerId(customerId);
        accounts.forEach(System.out::println);
    }

    private static void depositMoney() {
        System.out.print("Account ID: ");
        int accountId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Amount to deposit: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();

        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        account.deposit(amount);
        accountDAO.updateBalance(accountId, account.getBalance());

        // Record transaction
        Transaction transaction = new Transaction(accountId, Transaction.TransactionType.DEPOSIT, amount, account.getBalance(), "Deposit");
        transactionDAO.recordTransaction(transaction);

        System.out.println("Deposit successful! New balance: " + account.getBalance());
    }

    private static void withdrawMoney() {
        System.out.print("Account ID: ");
        int accountId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Amount to withdraw: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();

        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        if (account.withdraw(amount)) {
            accountDAO.updateBalance(accountId, account.getBalance());

            // Record transaction
            Transaction transaction = new Transaction(accountId, Transaction.TransactionType.WITHDRAWAL, amount, account.getBalance(), "Withdrawal");
            transactionDAO.recordTransaction(transaction);

            System.out.println("Withdrawal successful! New balance: " + account.getBalance());
        } else {
            System.out.println("Insufficient balance!");
        }
    }

    private static void updateAccountStatus() {
        System.out.print("Account ID: ");
        int accountId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("New Status (ACTIVE/INACTIVE/CLOSED): ");
        String statusStr = scanner.nextLine();

        try {
            Account.AccountStatus status = Account.AccountStatus.valueOf(statusStr.toUpperCase());
            accountDAO.updateAccountStatus(accountId, status);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status!");
        }
    }

    private static void deleteAccount() {
        System.out.print("Account ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        accountDAO.deleteAccount(id);
    }

    // ================= TRANSACTION MENU =================
    private static void transactionMenu() {
        System.out.println("\n--- TRANSACTION OPERATIONS ---");
        System.out.println("1. View Transactions by Account");
        System.out.println("2. View Recent Transactions");
        System.out.println("3. View All Transactions");
        System.out.print("Choose option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> viewTransactionsByAccount();
            case 2 -> viewRecentTransactions();
            case 3 -> viewAllTransactions();
            default -> System.out.println("Invalid option!");
        }
    }

    private static void viewTransactionsByAccount() {
        System.out.print("Account ID: ");
        int accountId = scanner.nextInt();
        scanner.nextLine();
        List<Transaction> transactions = transactionDAO.getTransactionsByAccountId(accountId);
        transactions.forEach(System.out::println);
    }

    private static void viewRecentTransactions() {
        System.out.print("Account ID: ");
        int accountId = scanner.nextInt();
        System.out.print("Number of recent transactions: ");
        int limit = scanner.nextInt();
        scanner.nextLine();

        List<Transaction> transactions = transactionDAO.getRecentTransactions(accountId, limit);
        transactions.forEach(System.out::println);
    }

    private static void viewAllTransactions() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        transactions.forEach(System.out::println);
    }
}
