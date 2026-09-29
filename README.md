# Bank of CLI

Bank of CLI is a terminal-based banking application built with Java, JDBC, and PostgreSQL.

The application supports authenticated Customer and Teller roles, core banking operations, transaction history, application logging, and live currency conversion through a public REST API.

## Features

### Customer
- Login with Account ID and 4-digit PIN
- Check account balance
- Deposit funds
- Withdraw funds with overdraft protection
- Transfer funds between accounts
- View recent transaction history
- Convert currencies using current exchange-rate data
- Logout

### Teller
- Login with Account ID and PIN
- Register new Customer accounts
- Logout

## Role-Based Access Control

Bank of CLI uses simple Role-Based Access Control (RBAC).

Each authenticated account has one of two roles:

```text
CUSTOMER
TELLER
```

The account role determines which application features the user can access.

```text
LOGIN
  |
  +-- CUSTOMER
  |      ├── Check Balance
  |      ├── Deposit Funds
  |      ├── Withdraw Funds
  |      ├── Transfer Funds
  |      ├── Transaction History
  |      ├── Currency Converter
  |      └── Logout
  |
  +-- TELLER
         ├── Register Customer Account
         └── Logout
```

New accounts registered by a Teller are assigned the `CUSTOMER` role by default.

## Architecture

The application uses a layered architecture:

```text
User
  ↓
BankRepl
API / Presentation Layer
  ↓
BankService / BankServiceImpl
Business Layer
  ↓
BankDAO / BankDAOImpl
Repository Layer
  ↓
JDBC
  ↓
PostgreSQL
```

The API layer handles terminal interaction.

The Service layer contains banking rules and validation.

The DAO layer handles database communication using JDBC and prepared statements.

The project specification refers to the database layer as the Repository Layer. This implementation uses the DAO pattern to fulfill that responsibility.

## Currency Converter

Bank of CLI connects to the Frankfurter public REST API to retrieve current currency exchange-rate data.

```text
Customer
   ↓
Currency Converter
   ↓
Java HttpClient
   ↓
Frankfurter REST API
   ↓
Exchange-rate response
   ↓
Converted amount
```

Example:

```text
Amount to convert: 100
From currency: USD
To currency: EUR

100.00 USD = 87.73 EUR
```

The currency converter is informational only and does not modify the customer's bank balance.

## Technologies

- Java 17
- Maven
- PostgreSQL
- JDBC
- Java HttpClient
- REST API integration
- JUnit 5
- Mockito
- Java I/O
- Git / GitHub

## Database

Bank of CLI uses two PostgreSQL tables.

### accounts

Stores:

- Account ID
- PIN
- Balance in cents
- Role

Example roles:

```text
CUSTOMER
TELLER
```

### transactions

Stores:

- Transaction ID
- Account ID
- Related Account ID
- Transaction type
- Amount in cents
- Timestamp

Transaction types include:

```text
DEPOSIT
WITHDRAWAL
TRANSFER_OUT
TRANSFER_IN
```

Money is stored as whole cents instead of floating-point values.

For example:

```text
$200.00 → 20000 cents
$25.50  → 2550 cents
```

## Atomic Transfers

Transfers use JDBC transaction control.

A transfer must successfully:

1. Debit the sender
2. Credit the receiver
3. Record `TRANSFER_OUT`
4. Record `TRANSFER_IN`

If every operation succeeds, the database transaction is committed.

If an operation fails, the transaction is rolled back so a partial transfer cannot occur.

## Testing

The project contains automated Service and Repository tests using JUnit 5 and Mockito.

```text
Service tests:       14
Repository tests:    12
Total tests:         26

Failures:             0
Errors:               0
Skipped:              0

BUILD SUCCESS
```

The final application workflow was also manually verified for:

- Customer login and role routing
- Teller login and role routing
- Teller-created Customer accounts
- Customer banking menu access
- Teller-only account registration
- Public API currency conversion

## Logging

Application activity is written to:

```text
logs/application.log
```

Successful actions use `INFO`.

Failed or invalid actions use `ERROR`.

PIN values are never written to the application log.

## Database Configuration

Create:

```text
src/main/resources/db.properties
```

Using:

```properties
DB_URL=jdbc:postgresql://localhost:5432/bankofcli
DB_USER=your_database_username
DB_PASSWORD=your_database_password
```

A safe template is included as:

```text
src/main/resources/db.properties.example
```

The real `db.properties` file is excluded from Git because it contains database credentials.

## Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE bankofcli;
```

The application initializes the required tables when the DAO starts.

Existing installations can add the RBAC role column with:

```sql
ALTER TABLE accounts
ADD COLUMN IF NOT EXISTS role VARCHAR(20)
NOT NULL DEFAULT 'CUSTOMER';
```

A Teller account can be created directly in PostgreSQL for demonstration purposes:

```sql
INSERT INTO accounts (
    account_id,
    pin,
    balance_cents,
    role
)
VALUES (
    900001,
    '5678',
    0,
    'TELLER'
);
```

## Run Tests

```bash
mvn clean test
```

Expected result:

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Run the Application

```bash
mvn clean compile exec:java "-Dexec.mainClass=com.bankofcli.api.Main"
```

## View PostgreSQL Data

Connect to PostgreSQL:

```bash
psql -h localhost -U your_database_username -d bankofcli
```

View accounts and assigned roles:

```sql
SELECT account_id, balance_cents, role
FROM accounts;
```

View transaction history:

```sql
SELECT *
FROM transactions;
```

## Security Notes

This project demonstrates authentication and basic role-based authorization within the scope of the course.

PIN values are stored directly in PostgreSQL for this educational implementation.

A production banking application would require additional protections such as:

- Password/PIN hashing
- Stronger authentication
- Session management
- Expanded authorization controls
- Secure secret management
- Encryption and production-grade auditing

## Key Concepts Demonstrated

- Java classes and objects
- Interfaces
- Enums
- Encapsulation
- Collections
- Loops
- Conditional statements
- Switch statements
- Exception handling
- Dependency injection
- Layered architecture
- DAO pattern
- Role-Based Access Control
- JDBC
- PostgreSQL
- Prepared statements
- Result sets
- SQL constraints
- Primary and foreign keys
- Database transactions
- Commit and rollback
- REST API integration
- Java HttpClient
- File I/O
- Application logging
- JUnit 5
- Mockito
- Positive and negative testing