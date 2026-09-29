package com.bankofcli.domain;

public class Account {

    // Unique ID used to identify the bank account.
    private long accountId;

    // Store the PIN as a String so leading zeros are preserved.
    private String pin;

    // Store money as whole cents.
    private long balanceCents;

    // Determines which features this account may access.
    private Role role;

    /*
     * Existing account creation defaults
     * to the CUSTOMER role.
     */
    public Account(
            long accountId,
            String pin,
            long balanceCents) {

        this(
                accountId,
                pin,
                balanceCents,
                Role.CUSTOMER);
    }

    /*
     * Used when the role is already known.
     */
    public Account(
            long accountId,
            String pin,
            long balanceCents,
            Role role) {

        this.accountId = accountId;
        this.pin = pin;
        this.balanceCents = balanceCents;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return String.format(
                "Account ID: %d | Balance: $%.2f",
                accountId,
                balanceCents / 100.0);
    }
}