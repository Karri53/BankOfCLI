package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.persistence.BankDAO;

import java.util.List;

public class BankServiceImpl implements BankService {

    // The Service Layer uses the DAO to access the database.
    private final BankDAO bankDAO;

    // Dependency injection allows the DAO to be provided to the service.
    public BankServiceImpl(BankDAO bankDAO) {
        this.bankDAO = bankDAO;
    }

    @Override
    public void register(long accountId, String pin) {

        // Account IDs must be positive.
        if (accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be greater than zero");
        }

        // Prevent duplicate account IDs.
        if (bankDAO.getAccountById(accountId) != null) {
            throw new IllegalArgumentException(
                    "Account ID already exists");
        }

        // Validate the PIN before storing the account.
        validatePin(pin);

        // New accounts begin with a zero balance.
        Account account = new Account(
                accountId,
                pin,
                0);

        bankDAO.addAccount(account);
    }

    @Override
    public Account login(long accountId, String pin) {

        // Retrieve the account from the database.
        Account account = bankDAO.getAccountById(accountId);

        /*
         * Reject the login if the account does not exist
         * or the PIN does not match.
         */
        if (account == null || !account.getPin().equals(pin)) {
            throw new IllegalArgumentException(
                    "Invalid account ID or PIN");
        }

        return account;
    }

    @Override
    public long getBalance(long accountId) {

        Account account = getExistingAccount(accountId);

        return account.getBalanceCents();
    }

    @Override
    public void deposit(long accountId, long amountCents) {

        // Deposits must contain a positive amount.
        if (amountCents <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        // Confirm the account exists before calling the DAO.
        getExistingAccount(accountId);

        bankDAO.deposit(
                accountId,
                amountCents);
    }

    @Override
    public void withdraw(long accountId, long amountCents) {

        // Withdrawal amounts must be positive.
        if (amountCents <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }

        Account account = getExistingAccount(accountId);

        // Business rule: an account cannot be overdrawn.
        if (account.getBalanceCents() < amountCents) {
            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        /*
         * The DAO also protects against overdrawing at the
         * database level. A false result means the withdrawal
         * could not be completed.
         */
        boolean successful =
                bankDAO.withdraw(
                        accountId,
                        amountCents);

        if (!successful) {
            throw new IllegalArgumentException(
                    "Withdrawal could not be completed");
        }
    }

    @Override
    public void transfer(
            long fromAccountId,
            long toAccountId,
            long amountCents) {

        // Transfer amounts must be positive.
        if (amountCents <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        // An account cannot transfer money to itself.
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to the same account");
        }

        // Confirm both accounts exist.
        Account sender =
                getExistingAccount(fromAccountId);

        getExistingAccount(toAccountId);

        // The sender must have enough money.
        if (sender.getBalanceCents() < amountCents) {
            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        /*
         * The DAO handles the actual database transaction
         * using commit and rollback.
         */
        bankDAO.transfer(
                fromAccountId,
                toAccountId,
                amountCents);
    }

    @Override
    public List<Transaction> getTransactionHistory(
            long accountId) {

        // Confirm the requested account exists.
        getExistingAccount(accountId);

        return bankDAO.getRecentTransactions(
                accountId);
    }

    /*
     * Reuse the account lookup logic instead of repeating
     * the same null check throughout the service.
     */
    private Account getExistingAccount(long accountId) {

        Account account =
                bankDAO.getAccountById(accountId);

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account not found");
        }

        return account;
    }

    /*
     * This project uses a simple four-digit PIN.
     * Character.isDigit keeps the validation beginner-friendly
     * without using regular expressions.
     */
    private void validatePin(String pin) {

        if (pin == null || pin.length() != 4) {
            throw new IllegalArgumentException(
                    "PIN must contain exactly 4 digits");
        }

        for (int i = 0; i < pin.length(); i++) {

            if (!Character.isDigit(pin.charAt(i))) {
                throw new IllegalArgumentException(
                        "PIN must contain exactly 4 digits");
            }
        }
    }
}