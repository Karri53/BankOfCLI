package com.bankofcli.domain;

public class Account {

    // Unique ID used to identify the bank account.
    private long accountId;

    // Store the PIN as a String so values such as "0123" keep the leading zero.
    private String pin;

    // Store money as whole cents instead of using floating-point values.
    private long balanceCents;

    public Account(long accountId, String pin, long balanceCents) {
        this.accountId = accountId;
        this.pin = pin;
        this.balanceCents = balanceCents;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getPin() {
        return pin;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    @Override
    public String toString() {
        return String.format(
                "Account ID: %d | Balance: $%.2f",
                accountId,
                balanceCents / 100.0);
    }
}