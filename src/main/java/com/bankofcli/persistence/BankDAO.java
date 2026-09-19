package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;

import java.util.List;

public interface BankDAO {

    // Store a newly registered account.
    void addAccount(Account account);

    // Find one account by its unique account ID.
    Account getAccountById(long accountId);

    // Add money to an account and record the transaction.
    void deposit(long accountId, long amountCents);

    // Remove money only when enough funds are available.
    boolean withdraw(long accountId, long amountCents);

    // Move money between two accounts as one database transaction.
    void transfer(long fromAccountId, long toAccountId, long amountCents);

    // Return the recent transaction history for one account.
    List<Transaction> getRecentTransactions(long accountId);
}