package com.bankofcli.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.persistence.BankDAO;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankServiceImplTest {

    private BankDAO bankDAO;
    private BankService service;

    /*
     * Create a fresh mocked DAO and Service before every test.
     * This keeps each unit test isolated from the database.
     */
    @BeforeEach
    void setUp() {
        bankDAO = mock(BankDAO.class);
        service = new BankServiceImpl(bankDAO);
    }


    // ---------------------------------------------------------
    // REGISTER TESTS
    // ---------------------------------------------------------

    @Test
    void registerCreatesNewAccount() {

        // Arrange
        long accountId = 1001L;

        // No existing account means this ID is available.
        when(bankDAO.getAccountById(accountId))
                .thenReturn(null);

        // Act
        service.register(accountId, "1234");

        // Assert
        verify(bankDAO).getAccountById(accountId);
        verify(bankDAO).addAccount(any(Account.class));
    }

    @Test
    void registerRejectsDuplicateAccountId() {

        // Arrange
        Account existingAccount =
                new Account(1001L, "1234", 0);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(existingAccount);

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.register(1001L, "5678"));

        // Assert
        assertEquals(
                "Account ID already exists",
                exception.getMessage());

        // The duplicate account should never be stored.
        verify(bankDAO, never())
                .addAccount(any(Account.class));
    }


    // ---------------------------------------------------------
    // LOGIN TESTS
    // ---------------------------------------------------------

    @Test
    void loginReturnsAccountWithCorrectPin() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        // Act
        Account result =
                service.login(1001L, "1234");

        // Assert
        assertEquals(account, result);
    }

    @Test
    void loginRejectsIncorrectPin() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.login(1001L, "9999"));

        // Assert
        assertEquals(
                "Invalid account ID or PIN",
                exception.getMessage());
    }


    // ---------------------------------------------------------
    // GET BALANCE TESTS
    // ---------------------------------------------------------

    @Test
    void getBalanceReturnsCurrentBalance() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        // Act
        long balance =
                service.getBalance(1001L);

        // Assert
        assertEquals(50000, balance);
    }

    @Test
    void getBalanceRejectsUnknownAccount() {

        // Arrange
        when(bankDAO.getAccountById(9999L))
                .thenReturn(null);

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getBalance(9999L));

        // Assert
        assertEquals(
                "Account not found",
                exception.getMessage());
    }


    // ---------------------------------------------------------
    // DEPOSIT TESTS
    // ---------------------------------------------------------

    @Test
    void depositAddsPositiveAmount() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        // Act
        service.deposit(1001L, 10000);

        // Assert
        verify(bankDAO)
                .deposit(1001L, 10000);
    }

    @Test
    void depositRejectsNonPositiveAmount() {

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.deposit(1001L, 0));

        // Assert
        assertEquals(
                "Deposit amount must be greater than zero",
                exception.getMessage());

        // Invalid deposits should never reach the DAO.
        verify(bankDAO, never())
                .deposit(1001L, 0);
    }


    // ---------------------------------------------------------
    // WITHDRAW TESTS
    // ---------------------------------------------------------

    @Test
    void withdrawRemovesMoneyWhenFundsAreAvailable() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        when(bankDAO.withdraw(1001L, 10000))
                .thenReturn(true);

        // Act
        service.withdraw(1001L, 10000);

        // Assert
        verify(bankDAO)
                .withdraw(1001L, 10000);
    }

    @Test
    void withdrawRejectsInsufficientFunds() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 5000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.withdraw(1001L, 10000));

        // Assert
        assertEquals(
                "Insufficient funds",
                exception.getMessage());

        // The DAO should never attempt an invalid withdrawal.
        verify(bankDAO, never())
                .withdraw(1001L, 10000);
    }


    // ---------------------------------------------------------
    // TRANSFER TESTS
    // ---------------------------------------------------------

    @Test
    void transferMovesMoneyBetweenAccounts() {

        // Arrange
        Account sender =
                new Account(1001L, "1234", 50000);

        Account receiver =
                new Account(2001L, "5678", 10000);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(sender);

        when(bankDAO.getAccountById(2001L))
                .thenReturn(receiver);

        // Act
        service.transfer(
                1001L,
                2001L,
                10000);

        // Assert
        verify(bankDAO)
                .transfer(
                        1001L,
                        2001L,
                        10000);
    }

    @Test
    void transferRejectsSameAccount() {

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.transfer(
                                1001L,
                                1001L,
                                10000));

        // Assert
        assertEquals(
                "Cannot transfer money to the same account",
                exception.getMessage());

        // Invalid transfers should never reach the DAO.
        verify(bankDAO, never())
                .transfer(
                        1001L,
                        1001L,
                        10000);
    }


    // ---------------------------------------------------------
    // TRANSACTION HISTORY TESTS
    // ---------------------------------------------------------

    @Test
    void getTransactionHistoryReturnsRecentTransactions() {

        // Arrange
        Account account =
                new Account(1001L, "1234", 50000);

        Transaction transaction =
                new Transaction(
                        1L,
                        1001L,
                        null,
                        "DEPOSIT",
                        10000,
                        LocalDateTime.now());

        List<Transaction> transactions =
                List.of(transaction);

        when(bankDAO.getAccountById(1001L))
                .thenReturn(account);

        when(bankDAO.getRecentTransactions(1001L))
                .thenReturn(transactions);

        // Act
        List<Transaction> result =
                service.getTransactionHistory(1001L);

        // Assert
        assertIterableEquals(
                transactions,
                result);
    }

    @Test
    void getTransactionHistoryRejectsUnknownAccount() {

        // Arrange
        when(bankDAO.getAccountById(9999L))
                .thenReturn(null);

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getTransactionHistory(9999L));

        // Assert
        assertEquals(
                "Account not found",
                exception.getMessage());

        // Do not query history for an account that does not exist.
        verify(bankDAO, never())
                .getRecentTransactions(9999L);
    }
}