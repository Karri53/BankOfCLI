package com.bankofcli.api;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.service.BankService;
import com.bankofcli.util.AppLogger;
import java.util.List;
import java.util.Scanner;

public class BankRepl {

    // The API layer communicates only with the Service layer.
    private final BankService service;

    // Scanner reads user input from the terminal.
    private final Scanner scanner = new Scanner(System.in);

    public BankRepl(BankService service) {
        this.service = service;
    }

    /*
     * Starts the application and keeps showing the main menu
     * until the user chooses to exit.
     */
    public void run() {

        System.out.println();
        System.out.println("===========================");
        System.out.println("        BANK OF CLI");
        System.out.println("===========================");

        while (true) {

            printMainMenu();

            try {

                int choice =
                        readInt("Choose an option: ");

                switch (choice) {

                    case 1 ->
                            register();

                    case 2 ->
                            login();

                    case 3 -> {

                        // Record a successful application exit.
                        AppLogger.info(
                                "Bank of CLI application exited");

                        System.out.println(
                                "Thank you for using Bank of CLI.");

                        return;
                    }

                    default ->
                            System.out.println(
                                    "Please choose a valid option.");
                }

            } catch (IllegalArgumentException e) {

                /*
                 * Record user input, validation,
                 * or security-related failures.
                 */
                AppLogger.error(
                        e.getMessage());

                // Give the user a simple message instead of a stack trace.
                System.out.println(
                        "Error: " + e.getMessage());

            } catch (IllegalStateException e) {

                /*
                 * Record database or system failures,
                 * but hide technical details from the user.
                 */
                AppLogger.error(
                        "System failure: "
                                + e.getMessage());

                System.out.println(
                        "Service unavailable. Please try again later.");
            }
        }
    }

    /*
     * Register a new account using an account ID and PIN.
     */
    private void register() {

        System.out.println();
        System.out.println("--- REGISTER ---");

        long accountId =
                readLong(
                        "Create an Account ID: ");

        String pin =
                readString(
                        "Create a 4-digit PIN: ");

        service.register(
                accountId,
                pin);

        // Record the successful registration without logging the PIN.
        AppLogger.info(
                "Account "
                        + accountId
                        + " registered successfully");

        System.out.println(
                "Account registered successfully.");
    }

    /*
     * Login using an existing account ID and PIN.
     */
    private void login() {

        System.out.println();
        System.out.println("--- LOGIN ---");

        long accountId =
                readLong(
                        "Account ID: ");

        String pin =
                readString(
                        "PIN: ");

        Account account =
                service.login(
                        accountId,
                        pin);

        // Record only successful logins.
        AppLogger.info(
                "Account "
                        + accountId
                        + " logged in successfully");

        System.out.println();
        System.out.println(
                "Login successful. Welcome to your account.");

        /*
         * After a successful login,
         * send the user to the account menu.
         */
        runAccountMenu(
                account.getAccountId());
    }

    /*
     * Shows banking options after the user logs in.
     */
    private void runAccountMenu(
            long accountId) {

        while (true) {

            printAccountMenu();

            try {

                int choice =
                        readInt(
                                "Choose an option: ");

                switch (choice) {

                    case 1 ->
                            checkBalance(accountId);

                    case 2 ->
                            deposit(accountId);

                    case 3 ->
                            withdraw(accountId);

                    case 4 ->
                            transfer(accountId);

                    case 5 ->
                            viewTransactionHistory(accountId);

                    case 6 -> {

                        // Record the successful logout.
                        AppLogger.info(
                                "Account "
                                        + accountId
                                        + " logged out");

                        System.out.println(
                                "You have been logged out.");

                        return;
                    }

                    default ->
                            System.out.println(
                                    "Please choose a valid option.");
                }

            } catch (IllegalArgumentException e) {

                // Record validation or user-related failures.
                AppLogger.error(
                        e.getMessage());

                // Keep technical details away from the terminal user.
                System.out.println(
                        "Error: " + e.getMessage());

            } catch (IllegalStateException e) {

                /*
                 * Record database or system failures,
                 * but give the user a simple message.
                 */
                AppLogger.error(
                        "System failure: "
                                + e.getMessage());

                System.out.println(
                        "Service unavailable. Please try again later.");
            }
        }
    }

    /*
     * Display the current account balance.
     */
    private void checkBalance(
            long accountId) {

        long balanceCents =
                service.getBalance(
                        accountId);

        // Record the successful balance request.
        AppLogger.info(
                "Account "
                        + accountId
                        + " viewed current balance");

        System.out.printf(
                "Current balance: $%.2f%n",
                balanceCents / 100.0);
    }

    /*
     * Deposit money into the logged-in account.
     */
    private void deposit(
            long accountId) {

        System.out.println();
        System.out.println("--- DEPOSIT ---");

        long amountCents =
                readAmountInCents(
                        "Deposit amount: $");

        service.deposit(
                accountId,
                amountCents);

        // Record the successful deposit.
        AppLogger.info(
                "Account "
                        + accountId
                        + " completed a deposit");

        System.out.printf(
                "Deposit of $%.2f completed successfully.%n",
                amountCents / 100.0);
    }

    /*
     * Withdraw money from the logged-in account.
     */
    private void withdraw(
            long accountId) {

        System.out.println();
        System.out.println("--- WITHDRAW ---");

        long amountCents =
                readAmountInCents(
                        "Withdrawal amount: $");

        service.withdraw(
                accountId,
                amountCents);

        // Record the successful withdrawal.
        AppLogger.info(
                "Account "
                        + accountId
                        + " completed a withdrawal");

        System.out.printf(
                "Withdrawal of $%.2f completed successfully.%n",
                amountCents / 100.0);
    }

    /*
     * Transfer money from the logged-in account
     * to another account.
     */
    private void transfer(
            long accountId) {

        System.out.println();
        System.out.println("--- TRANSFER ---");

        long receivingAccountId =
                readLong(
                        "Receiving Account ID: ");

        long amountCents =
                readAmountInCents(
                        "Transfer amount: $");

        service.transfer(
                accountId,
                receivingAccountId,
                amountCents);

        // Record the successful transfer.
        AppLogger.info(
                "Account "
                        + accountId
                        + " transferred funds to account "
                        + receivingAccountId);

        System.out.printf(
                "Transfer of $%.2f completed successfully.%n",
                amountCents / 100.0);
    }

    /*
     * Display the recent transaction history
     * returned by the Service layer.
     */
    private void viewTransactionHistory(
            long accountId) {

        System.out.println();
        System.out.println(
                "--- RECENT TRANSACTIONS ---");

        List<Transaction> transactions =
                service.getTransactionHistory(
                        accountId);

        // Record the successful history request.
        AppLogger.info(
                "Account "
                        + accountId
                        + " viewed transaction history");

        if (transactions.isEmpty()) {

            System.out.println(
                    "No transactions found.");

            return;
        }

        /*
         * Use an enhanced for loop to print
         * each Transaction object in the List.
         */
        for (Transaction transaction : transactions) {

            System.out.println(
                    transaction);
        }
    }

    /*
     * Main menu shown before login.
     */
    private void printMainMenu() {

        System.out.println();
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
    }

    /*
     * Account menu shown after login.
     */
    private void printAccountMenu() {

        System.out.println();
        System.out.println("===========================");
        System.out.println("       ACCOUNT MENU");
        System.out.println("===========================");
        System.out.println("1. Check Balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Transaction History");
        System.out.println("6. Logout");
    }

    /*
     * Read a whole-number menu option.
     * Invalid text is converted into a friendly error message.
     */
    private int readInt(
            String prompt) {

        System.out.print(
                prompt);

        try {

            return Integer.parseInt(
                    scanner.nextLine().trim());

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Please enter a valid menu number.");
        }
    }

    /*
     * Read an account ID.
     * Invalid text is converted into a friendly error message.
     */
    private long readLong(
            String prompt) {

        System.out.print(
                prompt);

        try {

            return Long.parseLong(
                    scanner.nextLine().trim());

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Please enter a valid Account ID.");
        }
    }

    /*
     * Read text such as a PIN.
     */
    private String readString(
            String prompt) {

        System.out.print(
                prompt);

        return scanner.nextLine().trim();
    }

    /*
     * Read a dollar amount and convert it to cents.
     *
     * Example:
     * $25.50 becomes 2550 cents.
     *
     * Invalid text is converted into
     * a friendly error message.
     */
    private long readAmountInCents(
            String prompt) {

        System.out.print(
                prompt);

        try {

            double amount =
                    Double.parseDouble(
                            scanner.nextLine().trim());

            return Math.round(
                    amount * 100);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Please enter a valid dollar amount.");
        }
    }
}