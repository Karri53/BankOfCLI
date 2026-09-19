package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;

import java.util.List;

public interface BankService {

    // Register a new bank account.
    void register(long accountId, String pin);

    // Verify an account ID and PIN.
    Account login(long accountId, String pin);

    // Return the current balance for an account.
    long getBalance(long accountId);

    // Add money to an account.
    void deposit(long accountId, long amountCents);

    // Remove money from an account.
    void withdraw(long accountId, long amountCents);

    // Move money from one account to another.
    void transfer(long fromAccountId, long toAccountId, long amountCents);

    // Return the recent transaction history for an account.
    List<Transaction> getTransactionHistory(long accountId);
}