package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankDAOImplTest {

    private BankDAO bankDAO;

    /*
     * These IDs are reserved for repository tests.
     * Using the same test IDs makes cleanup simple and repeatable.
     */
    private static final long ACCOUNT_ONE = 9001L;
    private static final long ACCOUNT_TWO = 9002L;
    private static final long ACCOUNT_THREE = 9003L;

    @BeforeEach
    void setUp() {

        /*
         * Creating the DAO also confirms the schema exists
         * before each repository test begins.
         */
        bankDAO = new BankDAOImpl();

        // Start every test with clean test data.
        cleanTestData();
    }

    @AfterEach
    void tearDown() {

        // Remove test records after every test.
        cleanTestData();
    }


    // ---------------------------------------------------------
    // ADD ACCOUNT TESTS
    // ---------------------------------------------------------

    @Test
    void addAccountStoresNewAccount() {

        // Arrange
        Account account =
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        0);

        // Act
        bankDAO.addAccount(account);

        Account savedAccount =
                bankDAO.getAccountById(ACCOUNT_ONE);

        // Assert
        assertNotNull(savedAccount);

        assertEquals(
                ACCOUNT_ONE,
                savedAccount.getAccountId());

        assertEquals(
                "1234",
                savedAccount.getPin());

        assertEquals(
                0,
                savedAccount.getBalanceCents());
    }

    @Test
    void addAccountRejectsDuplicateAccountId() {

        // Arrange
        Account firstAccount =
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        0);

        Account duplicateAccount =
                new Account(
                        ACCOUNT_ONE,
                        "5678",
                        0);

        bankDAO.addAccount(firstAccount);

        // Act + Assert
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> bankDAO.addAccount(
                                duplicateAccount));

        assertEquals(
                "Could not add account",
                exception.getMessage());
    }


    // ---------------------------------------------------------
    // GET ACCOUNT TESTS
    // ---------------------------------------------------------

    @Test
    void getAccountByIdReturnsExistingAccount() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        25000));

        // Act
        Account result =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        // Assert
        assertNotNull(result);

        assertEquals(
                ACCOUNT_ONE,
                result.getAccountId());

        assertEquals(
                25000,
                result.getBalanceCents());
    }

    @Test
    void getAccountByIdReturnsNullForUnknownAccount() {

        // Act
        Account result =
                bankDAO.getAccountById(
                        ACCOUNT_THREE);

        // Assert
        assertNull(result);
    }


    // ---------------------------------------------------------
    // DEPOSIT TESTS
    // ---------------------------------------------------------

    @Test
    void depositAddsMoneyToAccount() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        10000));

        // Act
        bankDAO.deposit(
                ACCOUNT_ONE,
                5000);

        Account result =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        // Assert
        assertEquals(
                15000,
                result.getBalanceCents());
    }

    @Test
    void depositRejectsUnknownAccount() {

        // Act
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> bankDAO.deposit(
                                ACCOUNT_THREE,
                                5000));

        // Assert
        assertEquals(
                "Account not found",
                exception.getMessage());
    }


    // ---------------------------------------------------------
    // WITHDRAW TESTS
    // ---------------------------------------------------------

    @Test
    void withdrawRemovesMoneyWhenFundsAreAvailable() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        10000));

        // Act
        boolean result =
                bankDAO.withdraw(
                        ACCOUNT_ONE,
                        4000);

        Account updatedAccount =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        // Assert
        assertTrue(result);

        assertEquals(
                6000,
                updatedAccount.getBalanceCents());
    }

    @Test
    void withdrawRejectsInsufficientFunds() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        5000));

        // Act
        boolean result =
                bankDAO.withdraw(
                        ACCOUNT_ONE,
                        10000);

        Account updatedAccount =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        // Assert
        assertFalse(result);

        // Balance should remain unchanged.
        assertEquals(
                5000,
                updatedAccount.getBalanceCents());
    }


    // ---------------------------------------------------------
    // TRANSFER TESTS
    // ---------------------------------------------------------

    @Test
    void transferMovesMoneyBetweenAccounts() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        10000));

        bankDAO.addAccount(
                new Account(
                        ACCOUNT_TWO,
                        "5678",
                        2000));

        // Act
        bankDAO.transfer(
                ACCOUNT_ONE,
                ACCOUNT_TWO,
                3000);

        Account sender =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        Account receiver =
                bankDAO.getAccountById(
                        ACCOUNT_TWO);

        // Assert
        assertEquals(
                7000,
                sender.getBalanceCents());

        assertEquals(
                5000,
                receiver.getBalanceCents());
    }

    @Test
    void transferRejectsInsufficientFunds() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        1000));

        bankDAO.addAccount(
                new Account(
                        ACCOUNT_TWO,
                        "5678",
                        2000));

        // Act
        assertThrows(
                IllegalStateException.class,
                () -> bankDAO.transfer(
                        ACCOUNT_ONE,
                        ACCOUNT_TWO,
                        5000));

        Account sender =
                bankDAO.getAccountById(
                        ACCOUNT_ONE);

        Account receiver =
                bankDAO.getAccountById(
                        ACCOUNT_TWO);

        // Assert
        // Neither balance should change.
        assertEquals(
                1000,
                sender.getBalanceCents());

        assertEquals(
                2000,
                receiver.getBalanceCents());
    }


    // ---------------------------------------------------------
    // TRANSACTION HISTORY TESTS
    // ---------------------------------------------------------

    @Test
    void getRecentTransactionsReturnsAccountHistory() {

        // Arrange
        bankDAO.addAccount(
                new Account(
                        ACCOUNT_ONE,
                        "1234",
                        0));

        bankDAO.deposit(
                ACCOUNT_ONE,
                5000);

        // Act
        List<Transaction> transactions =
                bankDAO.getRecentTransactions(
                        ACCOUNT_ONE);

        // Assert
        assertFalse(
                transactions.isEmpty());

        assertEquals(
                "DEPOSIT",
                transactions.get(0)
                        .getTransactionType());

        assertEquals(
                5000,
                transactions.get(0)
                        .getAmountCents());
    }

    @Test
    void getRecentTransactionsReturnsEmptyListForUnknownAccount() {

        // Act
        List<Transaction> transactions =
                bankDAO.getRecentTransactions(
                        ACCOUNT_THREE);

        // Assert
        assertTrue(
                transactions.isEmpty());
    }


    // ---------------------------------------------------------
    // TEST DATABASE CLEANUP
    // ---------------------------------------------------------

    /*
     * Repository tests use the real PostgreSQL database.
     * This method removes only the account IDs reserved for tests.
     */
    private void cleanTestData() {

        String deleteTransactionsSql = """
                DELETE FROM transactions
                WHERE account_id IN (?, ?, ?)
                OR related_account_id IN (?, ?, ?)
                """;

        String deleteAccountsSql = """
                DELETE FROM accounts
                WHERE account_id IN (?, ?, ?)
                """;

        try (
                Connection connection =
                        ConnectionFactory
                                .getConnectionFactory()
                                .getConnection();

                PreparedStatement transactionStatement =
                        connection.prepareStatement(
                                deleteTransactionsSql);

                PreparedStatement accountStatement =
                        connection.prepareStatement(
                                deleteAccountsSql)
        ) {

            // Remove transaction rows first because of foreign keys.
            transactionStatement.setLong(1, ACCOUNT_ONE);
            transactionStatement.setLong(2, ACCOUNT_TWO);
            transactionStatement.setLong(3, ACCOUNT_THREE);

            transactionStatement.setLong(4, ACCOUNT_ONE);
            transactionStatement.setLong(5, ACCOUNT_TWO);
            transactionStatement.setLong(6, ACCOUNT_THREE);

            transactionStatement.executeUpdate();

            // Then remove the test accounts.
            accountStatement.setLong(1, ACCOUNT_ONE);
            accountStatement.setLong(2, ACCOUNT_TWO);
            accountStatement.setLong(3, ACCOUNT_THREE);

            accountStatement.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not clean test data",
                    e);
        }
    }
}