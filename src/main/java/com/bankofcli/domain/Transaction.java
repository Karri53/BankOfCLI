package com.bankofcli.domain;

import java.time.LocalDateTime;

public class Transaction {

    // Unique ID for each transaction record.
    private long transactionId;

    // Account that owns this transaction record.
    private long accountId;

    // Stores the other account involved in a transfer.
    // Long is used instead of long because this can be null
    // for deposits and withdrawals.
    private Long relatedAccountId;

    // Examples: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT.
    private String transactionType;

    // Store the transaction amount as whole cents.
    private long amountCents;

    // Records when the transaction occurred.
    private LocalDateTime createdAt;

    public Transaction(
            long transactionId,
            long accountId,
            Long relatedAccountId,
            String transactionType,
            long amountCents,
            LocalDateTime createdAt) {

        this.transactionId = transactionId;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.transactionType = transactionType;
        this.amountCents = amountCents;
        this.createdAt = createdAt;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "Transaction ID: %d | Type: %s | Amount: $%.2f | Date: %s",
                transactionId,
                transactionType,
                amountCents / 100.0,
                createdAt);
    }
}