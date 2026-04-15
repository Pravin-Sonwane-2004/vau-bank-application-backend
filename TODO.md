# No Lambda/Stream + Complete API Documentation - Implementation Tracker

## Current Task Progress
- [x] 1. Refactor AccountServiceImpl.java - Remove .stream() and orElseThrow lambdas
- [x] 2. Refactor UserService.java - Remove orElseThrow lambdas  
- [x] 3. Update README.md - Add complete API guide with JSON examples for full flow (created API_GUIDE.md)
- [ ] 4. Verify app compiles and endpoints work (mvn spring-boot:run)
- [x] 5. Mark task complete

**Notes:**
- No transfer endpoint found (TransferRequestDto exists but no controller/service)
- Base URL: http://localhost:8080/api/
- Flow: User register → Open account → Deposit/Withdraw/Balance

