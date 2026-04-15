# 🚀 Complete VAUL Bank API Guide - User Creation to Money Transfer

**Base URL:** `http://localhost:8080`

**Key Features Implemented:**
- User registration
- Account opening with initial deposit
- Deposit, Withdraw, Balance check
- No lambdas/streams used in services (refactored to for-loops + Optional.isEmpty())

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

## Step 2: Open Bank Account

**Endpoint:** `POST /api/accounts/open`

**Request (use userId from Step 1):**
```json
{
  "userId": 1,
  "accountType": "SAVINGS",
  "initialDeposit": 1000.00,
  "branch": {
    "branchCode": 101
  }
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
  "branch": {"branchCode": 101}
}'
```

**Save `accountId` (10) for money operations.**

## Step 3: Send/Receive Money (Deposit & Withdraw)

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

## Step 4: Check Results

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

## Complete Test Flow (Copy-Paste Ready)

```bash
# 1. Register User
curl -X POST http://localhost:8080/api/users/add -H "Content-Type: application/json" -d '{"name":"Test User","email":"test@test.com","password":"pass","phone":"000","image":"test.jpg"}'

# 2. Open Account (use returned userId)
curl -X POST http://localhost:8080/api/accounts/open -H "Content-Type: application/json" -d '{"userId":1,"accountType":"SAVINGS","initialDeposit":1000,"branch":{"branchCode":101}}'

# 3. Deposit
curl -X POST http://localhost:8080/api/accounts/deposit -H "Content-Type: application/json" -d '{"accountId":1,"amount":500}'

# 4. Check Balance
curl http://localhost:8080/api/accounts/1/balance
```

## Notes
- **No Lambdas/Streams:** All services use traditional for-loops and Optional.isEmpty()
- **Transfer:** DTO exists but endpoint not implemented yet
- **Branch Codes:** Use ExistsBranches enum values (e.g. branchCode: 101)
- **Run App:** `mvn spring-boot:run` then test above

**Task Complete: Refactored code + Full documentation with JSON links!**

