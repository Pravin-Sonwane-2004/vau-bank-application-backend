# VAUL Bank Backend

Simple Spring Boot banking backend for interview practice and project demos.

This project focuses on the basic banking flow:

1. Register a user
2. Login with email and password
3. Open a bank account for that user
4. Deposit money
5. Withdraw money
6. Transfer money between accounts
7. Check balance
8. View transaction history

The code is kept straightforward on purpose so it is easy to explain in interviews. It uses normal controller -> service -> repository flow, simple loops and condition checks, and avoids unnecessary advanced patterns.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MySQL for the main app
- H2 for tests
- Maven

## Project Structure

```text
src/main/java/com/vaul/vaul
├── controllers
├── dtos
├── entities
├── enums
├── exceptions
├── repositories
├── services
└── configuration
```

## Main Modules

### 1. User Module

Handles customer registration and user updates.

Important files:

- `User`
- `UserController`
- `UserService`
- `UserRepo`

### 2. Account Module

Handles account opening, fetching account details, and balance.

Important files:

- `Account`
- `AccountController`
- `AccountService`
- `AccountRepository`

### 3. Transaction Module

Handles deposit, withdraw, transfer, and transaction history.

Important files:

- `Transaction`
- `TransactionRepository`
- methods inside `AccountServiceImpl`

## How the Flow Works

### Step 1: Register User

First create a user.

Endpoint:

`POST /api/users/add`

Example request:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "pass123",
  "phone": 9876543210
}
```

What happens internally:

1. Controller accepts request
2. Service validates email uniqueness
3. User is saved in database
4. Response DTO is returned

### Step 2: Login User

Endpoint:

`POST /api/users/login`

Example request:

```json
{
  "email": "john@example.com",
  "password": "pass123"
}
```

What happens internally:

1. Service finds user by email
2. Password is checked
3. If email or password is wrong, login fails
4. Basic user details are returned

Note:

This is a simple login for project flow. It does not generate JWT token.

### Step 3: Open Account

After user creation, open a bank account using that user id.

Endpoint:

`POST /api/accounts/open`

Example request:

```json
{
  "userId": 1,
  "accountType": "SAVINGS",
  "initialDeposit": 1000.00,
  "branch": "AMBAD"
}
```

What happens internally:

1. Service checks whether user exists
2. Service validates minimum opening deposit
3. Account number is generated in backend
4. Account is saved with status `ACTIVE`
5. Opening transaction is saved
6. Account response is returned

### Step 4: Deposit Money

Endpoint:

`POST /api/accounts/deposit`

Example request:

```json
{
  "accountId": 1,
  "amount": 500.00
}
```

Flow:

1. Find account
2. Check account is active
3. Add amount to balance
4. Save account
5. Save transaction record

### Step 5: Withdraw Money

Endpoint:

`POST /api/accounts/withdraw`

Example request:

```json
{
  "accountId": 1,
  "amount": 200.00
}
```

Flow:

1. Find account
2. Check account is active
3. Check enough balance is available
4. Subtract amount
5. Save account
6. Save transaction record

### Step 6: Transfer Money

Endpoint:

`POST /api/accounts/transfer`

Example request:

```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 300.00
}
```

Flow:

1. Validate both accounts are different
2. Lock both accounts for safe update
3. Check both accounts are active
4. Check sender has enough balance
5. Deduct from sender
6. Add to receiver
7. Save both accounts
8. Save one transfer transaction record

### Step 7: Check Balance

Endpoint:

`GET /api/accounts/{accountId}/balance`

This returns account id, account number, and current balance.

### Step 8: View Transactions

Endpoint:

`GET /api/accounts/{accountId}/transactions`

This returns deposit, withdraw, and transfer history for that account.

## Simple Architecture Explanation

This is the easiest way to explain the project in an interview:

- `Controller` handles HTTP request and response
- `Service` contains business logic
- `Repository` talks to database
- `DTO` is used for request and response payloads
- `Entity` represents database tables

Request flow:

```text
Client
 -> Controller
 -> Service
 -> Repository
 -> Database
 -> Response DTO
```

## Database Design

### User

Represents the person using the system.

Fields:

- `id`
- `name`
- `email`
- `password`
- `phone`
- `createdAt`

### Account

Represents a bank account owned by a user.

Fields:

- `id`
- `accountNumber`
- `accountType`
- `balance`
- `status`
- `branchCode`
- `openedAt`
- `user`

Relationship:

- One user can have many accounts

### Transaction

Represents money movement.

Fields:

- `id`
- `type`
- `amount`
- `fromAccountId`
- `toAccountId`
- `balanceAfter`
- `destinationBalanceAfter`
- `timestamp`
- `description`

## Available APIs

### User APIs

- `POST /api/users/login`
- `POST /api/users/add`
- `POST /api/users/bulk`
- `PUT /api/users/update/{id}`
- `PUT /api/users/updatebyemail/{email}`
- `GET /api/users/getall`
- `GET /api/users/get/{id}`

### Account APIs

- `POST /api/accounts/open`
- `GET /api/accounts/{accountId}`
- `GET /api/accounts/user/{userId}`
- `POST /api/accounts/deposit`
- `POST /api/accounts/withdraw`
- `POST /api/accounts/transfer`
- `GET /api/accounts/{accountId}/transactions`
- `GET /api/accounts/{accountId}/balance`

### Branch API

- `GET /api/branch`

Use this endpoint to see supported branch enum values such as `AMBAD`, `PARTUR`, and `JALNA_MAIN`.

## Business Rules

- Email must be unique for every user
- Account opening requires a valid user
- `SAVINGS` account minimum opening deposit is `1000.00`
- `CURRENT` account minimum opening deposit is `5000.00`
- Amount must be greater than zero for deposit, withdraw, and transfer
- Withdraw and transfer fail if balance is insufficient
- Money operations work only on `ACTIVE` accounts
- Every money change creates a transaction record

## Why `BigDecimal` Is Used

Money should not use `double` because floating-point values can create precision issues.

This project uses `BigDecimal` so balance calculations stay accurate.

## How to Run the Project

### 1. Create MySQL Database

Create a database named:

```sql
CREATE DATABASE vaul;
```

### 2. Check Database Config

The main config is in `src/main/resources/application.yaml`.

Default values:

- username: `root`
- password: `root`
- database: `vaul`
- port: `8080`

You can also override database credentials using:

- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### 3. Run the Application

```bash
mvn spring-boot:run
```

Base URL:

`http://localhost:8080`

## Simple End-to-End Test Flow

### 1. Register user

```bash
curl -X POST http://localhost:8080/api/users/add \
-H "Content-Type: application/json" \
-d "{\"name\":\"Test User\",\"email\":\"test@test.com\",\"password\":\"pass123\",\"phone\":9876543210}"
```

### 2. Login user

```bash
curl -X POST http://localhost:8080/api/users/login \
-H "Content-Type: application/json" \
-d "{\"email\":\"test@test.com\",\"password\":\"pass123\"}"
```

### 3. Open account

```bash
curl -X POST http://localhost:8080/api/accounts/open \
-H "Content-Type: application/json" \
-d "{\"userId\":1,\"accountType\":\"SAVINGS\",\"initialDeposit\":1000.00,\"branch\":\"AMBAD\"}"
```

### 4. Deposit money

```bash
curl -X POST http://localhost:8080/api/accounts/deposit \
-H "Content-Type: application/json" \
-d "{\"accountId\":1,\"amount\":500.00}"
```

### 5. Withdraw money

```bash
curl -X POST http://localhost:8080/api/accounts/withdraw \
-H "Content-Type: application/json" \
-d "{\"accountId\":1,\"amount\":200.00}"
```

### 6. Transfer money

```bash
curl -X POST http://localhost:8080/api/accounts/transfer \
-H "Content-Type: application/json" \
-d "{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":100.00}"
```

### 7. Check balance

```bash
curl http://localhost:8080/api/accounts/1/balance
```

### 8. View transactions

```bash
curl http://localhost:8080/api/accounts/1/transactions
```

## How to Explain This Project in an Interview

You can explain it like this:

1. This is a layered Spring Boot banking backend.
2. A user is registered first.
3. User can login with email and password.
4. A user can open one or more bank accounts.
5. Every deposit, withdraw, and transfer updates the account balance.
6. Every money operation is also saved as a transaction record.
7. Transfer uses `@Transactional` so debit and credit happen together.
8. `BigDecimal` is used for money values.
9. JPA repositories are used for database interaction.

## Good Interview Talking Points

- Why use `@Transactional` in transfer
- Why use `BigDecimal` for money
- Why keep `User`, `Account`, and `Transaction` separate
- Why store transaction history instead of only current balance
- Why validate minimum deposit and insufficient funds in service layer

## Current Scope

This project is intentionally simple.

It does not currently include:

- JWT security
- role-based authorization
- KYC workflow
- admin approval flow
- statement PDF generation
- external payment gateway integration

Those can be added later, but the current version is better for learning and interviews because the core flow is easy to understand.
