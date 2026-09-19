-- Create the accounts table.
CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT PRIMARY KEY,
    pin VARCHAR(4) NOT NULL,
    balance_cents BIGINT NOT NULL DEFAULT 0,

    -- Prevent the account from having a negative balance.
    CONSTRAINT check_balance_nonnegative
        CHECK (balance_cents >= 0)
);

-- Create the transactions table.
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Account that owns this transaction record.
    account_id BIGINT NOT NULL,

    -- Used only when another account is involved in a transfer.
    related_account_id BIGINT,

    -- Examples: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT.
    transaction_type VARCHAR(20) NOT NULL,

    -- Store all money as whole cents.
    amount_cents BIGINT NOT NULL,

    -- Automatically record when the transaction was created.
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ensure transaction amounts are always positive.
    CONSTRAINT check_amount_positive
        CHECK (amount_cents > 0),

    -- Connect each transaction to an account.
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(account_id),

    -- Connect transfers to the second account when one exists.
    CONSTRAINT fk_related_account
        FOREIGN KEY (related_account_id)
        REFERENCES accounts(account_id)
);