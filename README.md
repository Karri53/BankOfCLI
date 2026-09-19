Yep. The first README was way too much. We can keep the substance without turning the README into the PowerPoint. 😭

Your README should answer five things quickly: **What is this? What can it do? How is it built? How do I run it? How was it tested?**

Use this as the **full contents of `README.md`**:

````markdown
# Bank of CLI

Bank of CLI is a terminal-based banking application built with Java, JDBC, and PostgreSQL.

Users can register an account, log in with an Account ID and PIN, manage their balance, transfer money, and review recent transaction history.

## Features

- Account registration and PIN-based login
- Balance lookup
- Deposits
- Withdrawals with overdraft protection
- Transfers between accounts
- Recent transaction history
- INFO and ERROR application logging
- User-friendly error handling

## Architecture

The application uses a layered architecture:

```text
User
  ↓
BankRepl
API Layer
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
````

The API layer handles terminal interaction, the Service layer contains banking rules, and the DAO layer handles database communication.

The project specification refers to the database layer as the Repository Layer. This project implements that responsibility using the DAO pattern.

## Technologies

* Java 17
* Maven
* PostgreSQL
* JDBC
* JUnit 5
* Mockito
* Java I/O

## Database

Bank of CLI uses two PostgreSQL tables:

**accounts**

* Account ID
* PIN
* Balance stored in cents

**transactions**

* Transaction ID
* Account ID
* Related Account ID
* Transaction type
* Amount
* Timestamp

## Transaction types include:

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
3. Record TRANSFER_OUT
4. Record TRANSFER_IN

If every operation succeeds, the transaction is committed.

If an operation fails, the transaction is rolled back so partial transfers do not occur.

## Testing

The project follows the required two-test rule for Service and Repository methods.

```text
Service tests:       14
Repository tests:    12
Total tests:         26

Failures:             0
Errors:               0
Skipped:              0

BUILD SUCCESS
```

Service tests use JUnit 5 and Mockito.

Repository tests use JUnit 5 with PostgreSQL to verify JDBC and database behavior.

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

The real `db.properties` file is excluded from Git because it contains credentials.

## Run Tests

```bash
mvn clean test
```

## Run the Application

```bash
mvn exec:java "-Dexec.mainClass=com.bankofcli.api.Main"
```

## View PostgreSQL Data

Connect to the database:

```bash
psql -h localhost -U your_database_username -d bankofcli
```

View accounts:

```sql
SELECT * FROM accounts;
```

View transactions:

```sql
SELECT * FROM transactions;
```

## Security Note

This project stores PIN values directly in PostgreSQL to remain within the scope of the course implementation.

A production banking application would use secure credential hashing and additional authentication protections.

---

## Key Concepts Demonstrated
This project demonstrates:
Java classes and objects
Interfaces
Encapsulation
Collections
Loops
Conditional statements
Switch statements
Exception handling
Dependency injection
Layered architecture
DAO pattern
JDBC
PostgreSQL
Prepared statements
Result sets
SQL constraints
Primary keys
Foreign keys
Database transactions
Commit
Rollback
File I/O
Application logging
JUnit 5
Mockito
Positive testing
Negative testing