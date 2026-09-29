package com.bankofcli.api;

import java.util.List;
import java.util.Scanner;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Role;
import com.bankofcli.domain.Transaction;
import com.bankofcli.service.BankService;
import com.bankofcli.util.AppLogger;
import com.bankofcli.util.CurrencyConverter;

public class BankRepl {

        // The API layer communicates only with the Service layer.
        private final BankService service;

        // Retrieves current exchange rates from a public API.
        private final CurrencyConverter currencyConverter = new CurrencyConverter();

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

                // Display the application branding when the program starts.
                printWelcomeBanner();

                while (true) {

                        printMainMenu();

                        try {

                                int choice = readInt("Choose an option: ");

                                switch (choice) {

                                        case 1 ->
                                                login();

                                        case 2 -> {

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
         * Displays the Bank of CLI branding
         * when the application starts.
         */
        private void printWelcomeBanner() {

                System.out.println();
                System.out.println("==================================================");
                System.out.println("                 BANK OF CLI");
                System.out.println("              SIMPLE. SECURE. CLI.");
                System.out.println("==================================================");
                System.out.println();
                System.out.println("        ________________________________________");
                System.out.println("       |                                        |");
                System.out.println("       |                Welcome to              |");
                System.out.println("       |        Karrington's BANK OF CLI        |");
                System.out.println("       |________________________________________|");
                System.out.println("                      |          |");
                System.out.println("                      |   $$$$   |");
                System.out.println("                      |__________|");
                System.out.println();
                System.out.println("Welcome to Bank of CLI!");
        }

        /*
         * Register a new account using an account ID and PIN.
         */
        private void register() {

                System.out.println();
                System.out.println("--- REGISTER ---");

                long accountId = readLong(
                                "Create an Account ID: ");

                String pin = readString(
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

                long accountId = readLong(
                                "Account ID: ");

                String pin = readString(
                                "PIN: ");

                Account account = service.login(
                                accountId,
                                pin);

                // Record only successful logins.
                AppLogger.info(
                                "Account "
                                                + accountId
                                                + " logged in successfully");

                System.out.println();
                System.out.println("------------------------------------------");
                System.out.println("LOGIN SUCCESSFUL");
                System.out.println("------------------------------------------");
                System.out.println(
                                "Welcome back, Account "
                                                + account.getAccountId()
                                                + "!");


                /*
                 * Send the authenticated user to the menu
                 * allowed for their assigned role.
                 */
                if (account.getRole() == Role.TELLER) {

                        runTellerMenu(
                                        account.getAccountId());

                } else {

                        runAccountMenu(
                                        account.getAccountId());
                }
        }

        /*
         * Shows banking options after the user logs in.
         */
        private void runAccountMenu(
                        long accountId) {

                while (true) {

                        printAccountMenu(accountId);

                        try {

                                int choice = readInt(
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

                                        case 6 ->
                                                currencyConverter();

                                        case 7 -> {

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
         * Shows the limited options available
         * to an authenticated Teller.
         */
        private void runTellerMenu(
                        long accountId) {

                while (true) {

                        printTellerMenu(accountId);

                        try {

                                int choice = readInt(
                                                "Choose an option: ");

                                switch (choice) {

                                        case 1 ->
                                                register();

                                        case 2 -> {

                                                AppLogger.info(
                                                                "Teller "
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

                                AppLogger.error(
                                                e.getMessage());

                                System.out.println(
                                                "Error: "
                                                                + e.getMessage());

                        } catch (IllegalStateException e) {

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

                long balanceCents = service.getBalance(
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

                long amountCents = readAmountInCents(
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

                long amountCents = readAmountInCents(
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

                long receivingAccountId = readLong(
                                "Receiving Account ID: ");

                long amountCents = readAmountInCents(
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

                List<Transaction> transactions = service.getTransactionHistory(
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
         * Convert money between two currencies
         * using current exchange-rate data.
         */
        private void currencyConverter() {

                System.out.println();
                System.out.println("------------------------------------------");
                System.out.println("             CURRENCY CONVERTER");
                System.out.println("------------------------------------------");

                double amount;

                try {

                        System.out.print(
                                        "Amount to convert: ");

                        amount = Double.parseDouble(
                                        scanner.nextLine().trim());

                } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                        "Please enter a valid amount.");
                }

                String fromCurrency = readString(
                                "From currency (USD, EUR, GBP, etc.): ")
                                .toUpperCase();

                String toCurrency = readString(
                                "To currency (USD, EUR, GBP, etc.): ")
                                .toUpperCase();

                double convertedAmount = currencyConverter.convert(
                                amount,
                                fromCurrency,
                                toCurrency);

                System.out.println();
                System.out.println("------------------------------------------");

                System.out.printf(
                                "%.2f %s = %.2f %s%n",
                                amount,
                                fromCurrency,
                                convertedAmount,
                                toCurrency);

                System.out.println("------------------------------------------");

                AppLogger.info(
                                "Currency conversion completed from "
                                                + fromCurrency
                                                + " to "
                                                + toCurrency);
        }

        /*
         * Main menu shown before login.
         */
        private void printMainMenu() {

                System.out.println();
                System.out.println("------------------------------------------");
                System.out.println("                 MAIN MENU");
                System.out.println("------------------------------------------");
                System.out.println("1. Login");
                System.out.println("2. Exit");
                System.out.println("------------------------------------------");
        }

        /*
         * Account menu shown after login.
         */
        private void printAccountMenu(
                        long accountId) {

                System.out.println();
                System.out.println("==================================================");
                System.out.println("              CUSTOMER DASHBOARD");
                System.out.println("==================================================");
                System.out.println(
                                "Welcome back, Account "
                                                + accountId
                                                + ".");
                System.out.println("Role: CUSTOMER");
                System.out.println();
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit Funds");
                System.out.println("3. Withdraw Funds");
                System.out.println("4. Transfer Funds");
                System.out.println("5. View Transaction History");
                System.out.println("6. Currency Converter");
                System.out.println("7. Logout");
                System.out.println("------------------------------------------");
        }

        /*
         * Display the operations allowed
         * for a Teller account.
         */
        private void printTellerMenu(
                        long accountId) {

                System.out.println();
                System.out.println("==================================================");
                System.out.println("               TELLER DASHBOARD");
                System.out.println("==================================================");
                System.out.println(
                                "Welcome, Teller "
                                                + accountId
                                                + ".");
                System.out.println("Role: TELLER");
                System.out.println();
                System.out.println("1. Register Customer Account");
                System.out.println("2. Logout");
                System.out.println("------------------------------------------");
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

                        double amount = Double.parseDouble(
                                        scanner.nextLine().trim());

                        return Math.round(
                                        amount * 100);

                } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                        "Please enter a valid dollar amount.");
                }
        }
}