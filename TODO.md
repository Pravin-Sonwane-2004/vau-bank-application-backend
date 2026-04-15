# Banking Features Implementation TODO

## Phase 1: Enum Branches Integration & Account Operations
- [x] 1.1 Update AccountOpenRequestDto to use ExistsBranches enum instead of String branchCode
- [ ] 1.2 Update Account entity: Change branchCode from String to int branchCodeInt, store enum code
- [ ] 1.3 Update AccountRepository if needed for branch queries
- [ ] 1.4 Update AccountService interface: Add validation notes (no new methods yet)
- [x] 1.5 Update AccountServiceImpl.openAccount: Validate ExistsBranches, store .getBranchCode()
- [ ] 1.6 Update DTOs (AccountResponseDto): Add branchName or branchCode display
- [ ] ~~1.7 Test Phase 1 endpoints~~

## Phase 2: Deposit, Withdraw, Balance Check
- [x] 2.1 Create TransactionType enum
- [x] 2.2 Create Transaction entity & Repository
- [x] 2.3 Add DTOs for deposit/withdraw/balance
- [x] 2.4 Add service methods + Controller endpoints
- [x] 2.5 Implement logic with validations/transactions

## Phase 3: Transfer, History, Monthly Statement
- [ ] 3.1 Add transfer service method
- [ ] 3.2 Add history/statement methods with pagination
- [ ] 3.3 Update endpoints

## Phase 4: Account Status, Exceptions, Pagination
- [ ] 4.1 Add close/block/freeze methods
- [ ] 4.2 Create banking exceptions
- [ ] 4.3 Ensure pagination everywhere needed
- [ ] 4.4 Tests & final cleanup

**Current Progress: Starting Phase 1**
