package com.bankofcli.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Role;
import com.bankofcli.domain.Transaction;

public class BankDAOImpl implements BankDAO {

    /*
     * SQL used to create the accounts table.
     * The CHECK constraint prevents a negative balance.
     */
    private static final String CREATE_ACCOUNTS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_id BIGINT PRIMARY KEY,
                pin VARCHAR(4) NOT NULL,
                balance_cents BIGINT NOT NULL DEFAULT 0,
                role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',

                CONSTRAINT check_balance_nonnegative
                    CHECK (balance_cents >= 0)
            )
            """;

    /*
     * SQL used to create the transaction history table.
     * Each transaction belongs to an account.
     */
    private static final String CREATE_TRANSACTIONS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                account_id BIGINT NOT NULL,
                related_account_id BIGINT,
                transaction_type VARCHAR(20) NOT NULL,
                amount_cents BIGINT NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                CONSTRAINT check_amount_positive
                    CHECK (amount_cents > 0),

                CONSTRAINT fk_transaction_account
                    FOREIGN KEY (account_id)
                    REFERENCES accounts(account_id),

                CONSTRAINT fk_related_account
                    FOREIGN KEY (related_account_id)
                    REFERENCES accounts(account_id)
            )
            """;

    // SQL for creating a new bank account.
    private static final String INSERT_ACCOUNT_SQL = """
            INSERT INTO accounts (
                account_id,
                pin,
                balance_cents,
                role
            )
            VALUES (?, ?, ?, ?)
            """;

    // SQL for finding one account by its ID.
    private static final String FIND_ACCOUNT_SQL = """
            SELECT account_id,
                   pin,
                   balance_cents,
                   role
            FROM accounts
            WHERE account_id = ?
            """;

    // SQL for adding money to an account.
    private static final String DEPOSIT_SQL = """
            UPDATE accounts
            SET balance_cents = balance_cents + ?
            WHERE account_id = ?
            """;

    /*
     * SQL for removing money.
     * The final condition prevents the balance from going below zero.
     */
    private static final String WITHDRAW_SQL = """
            UPDATE accounts
            SET balance_cents = balance_cents - ?
            WHERE account_id = ?
            AND balance_cents >= ?
            """;

    // SQL used to credit money during a transfer.
    private static final String CREDIT_ACCOUNT_SQL = """
            UPDATE accounts
            SET balance_cents = balance_cents + ?
            WHERE account_id = ?
            """;

    // SQL used to save transaction history.
    private static final String INSERT_TRANSACTION_SQL = """
            INSERT INTO transactions (
                account_id,
                related_account_id,
                transaction_type,
                amount_cents
            )
            VALUES (?, ?, ?, ?)
            """;

    // Return the 10 most recent transactions for one account.
    private static final String FIND_RECENT_TRANSACTIONS_SQL = """
            SELECT transaction_id,
                   account_id,
                   related_account_id,
                   transaction_type,
                   amount_cents,
                   created_at
            FROM transactions
            WHERE account_id = ?
            ORDER BY created_at DESC
            LIMIT 10
            """;

    /*
     * When the DAO is created, make sure the required tables exist.
     * This follows the same approach used in the instructor example.
     */
    public BankDAOImpl() {
        initializeSchema();
    }

    @Override
    public void addAccount(Account account) {

        try (
                Connection connection = ConnectionFactory.getConnectionFactory().getConnection();

                PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_SQL)) {

            statement.setLong(1, account.getAccountId());
            statement.setString(2, account.getPin());
            statement.setLong(3, account.getBalanceCents());
            statement.setString(4, account.getRole().name());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw databaseError(
                    "Could not add account",
                    e);
        }
    }

    @Override
    public Account getAccountById(long accountId) {

        try (
                Connection connection = ConnectionFactory.getConnectionFactory().getConnection();

                PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT_SQL)) {

            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {

            throw databaseError(
                    "Could not find account",
                    e);
        }
    }

    @Override
    public void deposit(long accountId, long amountCents) {

        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {

            /*
             * Use one database transaction so the balance update
             * and transaction history are saved together.
             */
            connection.setAutoCommit(false);

            try (
                    PreparedStatement statement = connection.prepareStatement(DEPOSIT_SQL)) {

                statement.setLong(1, amountCents);
                statement.setLong(2, accountId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    connection.rollback();
                    throw new IllegalStateException("Account not found");
                }

                // Record the successful deposit in the audit trail.
                addTransaction(
                        connection,
                        accountId,
                        null,
                        "DEPOSIT",
                        amountCents);

                connection.commit();

            } catch (SQLException e) {

                // Undo the balance change if any database step fails.
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw databaseError(
                    "Could not complete deposit",
                    e);
        }
    }

    @Override
    public boolean withdraw(long accountId, long amountCents) {

        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement statement = connection.prepareStatement(WITHDRAW_SQL)) {

                statement.setLong(1, amountCents);
                statement.setLong(2, accountId);
                statement.setLong(3, amountCents);

                int rowsAffected = statement.executeUpdate();

                /*
                 * Zero affected rows means either the account was not found
                 * or the account did not have enough money.
                 */
                if (rowsAffected == 0) {
                    connection.rollback();
                    return false;
                }

                // Record the successful withdrawal.
                addTransaction(
                        connection,
                        accountId,
                        null,
                        "WITHDRAWAL",
                        amountCents);

                connection.commit();

                return true;

            } catch (SQLException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw databaseError(
                    "Could not complete withdrawal",
                    e);
        }
    }

    @Override
    public void transfer(
            long fromAccountId,
            long toAccountId,
            long amountCents) {

        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {

            /*
             * Turn off auto-commit so the transfer becomes
             * one atomic database transaction.
             */
            connection.setAutoCommit(false);

            try (
                    PreparedStatement debitStatement = connection.prepareStatement(WITHDRAW_SQL);

                    PreparedStatement creditStatement = connection.prepareStatement(CREDIT_ACCOUNT_SQL)) {

                // Step 1: Remove money from the sender.
                debitStatement.setLong(1, amountCents);
                debitStatement.setLong(2, fromAccountId);
                debitStatement.setLong(3, amountCents);

                int debitRows = debitStatement.executeUpdate();

                if (debitRows == 0) {
                    connection.rollback();

                    throw new IllegalStateException(
                            "Transfer could not be completed");
                }

                // Step 2: Add money to the receiving account.
                creditStatement.setLong(1, amountCents);
                creditStatement.setLong(2, toAccountId);

                int creditRows = creditStatement.executeUpdate();

                if (creditRows == 0) {
                    connection.rollback();

                    throw new IllegalStateException(
                            "Receiving account not found");
                }

                // Step 3: Record the sender's transaction.
                addTransaction(
                        connection,
                        fromAccountId,
                        toAccountId,
                        "TRANSFER_OUT",
                        amountCents);

                // Step 4: Record the receiver's transaction.
                addTransaction(
                        connection,
                        toAccountId,
                        fromAccountId,
                        "TRANSFER_IN",
                        amountCents);

                /*
                 * All four operations succeeded.
                 * Save everything permanently.
                 */
                connection.commit();

            } catch (SQLException e) {

                /*
                 * If any SQL operation fails, undo the entire transfer.
                 * This protects the Atomicity requirement.
                 */
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw databaseError(
                    "Could not complete transfer",
                    e);
        }
    }

    @Override
    public List<Transaction> getRecentTransactions(long accountId) {

        List<Transaction> transactions = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getConnectionFactory().getConnection();

                PreparedStatement statement = connection.prepareStatement(FIND_RECENT_TRANSACTIONS_SQL)) {

            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(
                            mapTransaction(resultSet));
                }
            }

            return transactions;

        } catch (SQLException e) {

            throw databaseError(
                    "Could not retrieve transaction history",
                    e);
        }
    }

    /*
     * Create the database tables when the DAO starts.
     * Accounts must be created first because transactions
     * contains foreign keys that reference accounts.
     */
    private void initializeSchema() {

        try (
                Connection connection = ConnectionFactory.getConnectionFactory().getConnection();

                PreparedStatement accountStatement = connection.prepareStatement(
                        CREATE_ACCOUNTS_TABLE_SQL);

                PreparedStatement transactionStatement = connection.prepareStatement(
                        CREATE_TRANSACTIONS_TABLE_SQL)) {

            accountStatement.executeUpdate();
            transactionStatement.executeUpdate();

        } catch (SQLException e) {

            throw databaseError(
                    "Could not initialize database schema",
                    e);
        }
    }

    /*
     * Save one row to the transaction history.
     * The existing connection is reused so this can participate
     * in the same database transaction as a deposit, withdrawal,
     * or transfer.
     */
    private void addTransaction(
            Connection connection,
            long accountId,
            Long relatedAccountId,
            String transactionType,
            long amountCents) throws SQLException {

        try (
                PreparedStatement statement = connection.prepareStatement(
                        INSERT_TRANSACTION_SQL)) {

            statement.setLong(1, accountId);

            /*
             * Deposits and withdrawals have no related account,
             * so SQL NULL is used in that situation.
             */
            if (relatedAccountId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, relatedAccountId);
            }

            statement.setString(3, transactionType);
            statement.setLong(4, amountCents);

            statement.executeUpdate();
        }
    }

    // Convert one SQL account row into an Account Java object.
    private Account mapAccount(
            ResultSet resultSet)
            throws SQLException {

        return new Account(
                resultSet.getLong("account_id"),
                resultSet.getString("pin"),
                resultSet.getLong("balance_cents"),
                Role.valueOf(
                        resultSet.getString("role")));
    }

    // Convert one SQL transaction row into a Transaction Java object.
    private Transaction mapTransaction(ResultSet resultSet)
            throws SQLException {

        /*
         * getObject() is used for related_account_id because
         * that database value may be NULL.
         */
        Long relatedAccountId = resultSet.getObject(
                "related_account_id",
                Long.class);

        return new Transaction(
                resultSet.getLong("transaction_id"),
                resultSet.getLong("account_id"),
                relatedAccountId,
                resultSet.getString("transaction_type"),
                resultSet.getLong("amount_cents"),
                resultSet.getTimestamp("created_at")
                        .toLocalDateTime());
    }

    // Convert SQL failures into one consistent application exception.
    private IllegalStateException databaseError(
            String message,
            SQLException cause) {

        return new IllegalStateException(
                message,
                cause);
    }
}