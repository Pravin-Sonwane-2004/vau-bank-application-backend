# 🚀 Complete VAUL Bank API Guide - User Creation to Money Transfer

**Base URL:** `http://localhost:8080`

**Key Features Implemented:**
- User registration
- Simple login with email and password
- Account opening with initial deposit
- Deposit, Withdraw, Transfer, Balance check
- Consistent success/error messages for API responses

**Full Flow Documentation:**

## Step 1: Create User

**Endpoint:** `POST /api/users/add`

**Request Example:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "pass123",
  "phone": "1234567890",
  "image": "avatar.jpg"
}
```

**Response Example:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "image": "avatar.jpg", 
  "phone": "1234567890"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/users/add \\
-H "Content-Type: application/json" \\
-d '{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "pass123",
  "phone": "1234567890",
  "image": "avatar.jpg"
}'
```

**Save the `id` (userId = 1) for next step.**

## Step 2: Login User

**Endpoint:** `POST /api/users/login`

**Request Example:**
```json
{
  "email": "john@example.com",
  "password": "pass123"
}
```

**Response Example:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": 1234567890,
  "message": "Login successful"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/users/login \
-H "Content-Type: application/json" \
-d '{
  "email": "john@example.com",
  "password": "pass123"
}'
```

**Note:** This is a basic login flow for project learning. It checks email and password, but does not create JWT token.

## Step 3: Open Bank Account

**Endpoint:** `POST /api/accounts/open`

**Request (use userId from Step 1):**
```json
{
  "userId": 1,
  "accountType": "SAVINGS",
  "initialDeposit": 1000.00,
  "branch": "AMBAD"
}
```

**Response:**
```json
{
  "accountId": 10,
  "accountNumber": "202400000123",
  "accountType": "SAVINGS",
  "balance": 1000.00,
  "status": "ACTIVE",
  "branchCode": 101,
  "openedAt": "2024-01-20T12:00:00",
  "userId": 1,
  "userName": "John Doe"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/accounts/open \\
-H "Content-Type: application/json" \\
-d '{
  "userId": 1,
  "accountType": "SAVINGS",
  "initialDeposit": 1000.00,
  "branch": "AMBAD"
}'
```

**Save `accountId` (10) for money operations.**

## Step 4: Send/Receive Money (Deposit & Withdraw)

### 3.1 Deposit Money

**Endpoint:** `POST /api/accounts/deposit`

**Request:**
```json
{
  "accountId": 10,
  "amount": 500.00
}
```

**Response:** Updated account (balance = 1500.00)

**cURL:**
```bash
curl -X POST http://localhost:8080/api/accounts/deposit \\
-H "Content-Type: application/json" \\
-d '{"accountId": 10, "amount": 500.00}'
```

### 3.2 Withdraw Money

**Endpoint:** `POST /api/accounts/withdraw`

**Request:**
```json
{
  "accountId": 10,
  "amount": 200.00
}
```

**Response:** Updated account (balance = 1300.00)

**cURL:**
```bash
curl -X POST http://localhost:8080/api/accounts/withdraw \\
-H "Content-Type: application/json" \\
-d '{"accountId": 10, "amount": 200.00}'
```

### 3.3 Transfer Money

**Endpoint:** `POST /api/accounts/transfer`

**Request:**
```json
{
  "fromAccountId": 10,
  "toAccountId": 11,
  "amount": 300.00
}
```

**Response:**
```json
{
  "id": 7,
  "type": "TRANSFER",
  "amount": 300.00,
  "fromAccountId": 10,
  "toAccountId": 11,
  "balanceAfter": 1000.00,
  "destinationBalanceAfter": 2300.00,
  "timestamp": "2026-04-24T10:00:00",
  "description": "Money transferred successfully",
  "message": "Money transferred successfully"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/accounts/transfer \\
-H "Content-Type: application/json" \\
-d '{"fromAccountId": 10, "toAccountId": 11, "amount": 300.00}'
```

## Step 5: Check Results

### 4.1 Get Balance

**Endpoint:** `GET /api/accounts/{accountId}/balance`

**cURL:**
```bash
curl http://localhost:8080/api/accounts/10/balance
```

**Response:**
```json
{
  "accountId": 10,
  "accountNumber": "202400000123",
  "balance": 1300.00
}
```

### 4.2 Get Account Details

**GET** `/api/accounts/10`

### 4.3 Get All User Accounts

**GET** `/api/accounts/user/1`

### 4.4 Get Transaction History

**GET** `/api/accounts/10/transactions`

## Complete Test Flow (Copy-Paste Ready)

```bash
# 1. Register User
curl -X POST http://localhost:8080/api/users/add -H "Content-Type: application/json" -d '{"name":"Test User","email":"test@test.com","password":"pass","phone":"000","image":"test.jpg"}'

# 2. Login User
curl -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d '{"email":"test@test.com","password":"pass"}'

# 3. Open Account (use returned userId)
curl -X POST http://localhost:8080/api/accounts/open -H "Content-Type: application/json" -d '{"userId":1,"accountType":"SAVINGS","initialDeposit":1000,"branch":"AMBAD"}'

# 4. Deposit
curl -X POST http://localhost:8080/api/accounts/deposit -H "Content-Type: application/json" -d '{"accountId":1,"amount":500}'

# 5. Transfer
curl -X POST http://localhost:8080/api/accounts/transfer -H "Content-Type: application/json" -d '{"fromAccountId":1,"toAccountId":2,"amount":200}'

# 6. Check Balance
curl http://localhost:8080/api/accounts/1/balance
```

## Notes
- **Transfer:** Fully implemented with transaction logging
- **Messages:** Mutating APIs now return success messages and errors return structured message payloads
- **Branch:** Use ExistsBranches enum values such as `AMBAD`, `JALNA_MAIN`, `PARTUR`
- **Run App:** `mvn spring-boot:run` then test above

**Task Complete: Transfer flow, cleaner services, and updated API documentation.**

