# Banking Application Services Specification - Mini-Banckito

This document details the 20 application and domain services designed for the Mini-Banckito architecture, categorized by business domain.

---

## 1. Account & Balance Management
1. **`CreateAccountService`**: Handles account creation, validating customer existence and setting initial deposit constraints.
2. **`GetAccountBalanceService`**: Retrieves real-time account balances encapsulated in `Money` value objects.
3. **`DepositService`**: Processes incoming credit transactions into specified bank accounts.
4. **`WithdrawalService`**: Executes fund withdrawals while validating balance availability to prevent overdrafts.
5. **`CloseAccountService`**: Terminates active accounts after verifying zero balance status.
6. **`FreezeAccountService`**: Temporarily blocks account operations upon detecting suspicious activity.
7. **`UnfreezeAccountService`**: Restores full operational privileges to frozen accounts following security clearance.

---

## 2. Transfers & Payments
8. **`InternalTransferService`**: Executes real-time fund transfers between accounts within Mini-Banckito.
9. **`InterbankTransferService`**: Orchestrates outgoing fund transfers to external financial institutions.
10. **`ScheduleTransferService`**: Schedules deferred or recurring periodic transfers.
11. **`CancelScheduledTransferService`**: Revokes pending scheduled transfers prior to execution time.
12. **`PayUtilityBillService`**: Processes payments for enrolled public utilities and agreements.

---

## 3. Customer Management
13. **`RegisterCustomerService`**: Onboards new individual customers into the system.
14. **`UpdateCustomerEmailService`**: Updates primary contact email addresses enforcing `Email` VO rules.
15. **`UpdateCustomerPhoneService`**: Updates verified contact telephone numbers.
16. **`GetCustomerProfileService`**: Returns consolidated profile details and associated active accounts.

---

## 4. Audit, Compliance & Reporting
17. **`GetTransactionHistoryService`**: Queries chronological transaction logs and movement history.
18. **`ValidateAccountStatusService`**: Checks operational status (Active, Frozen, Inactive).
19. **`GenerateAccountStatementService`**: Generates monthly account statement reports.
20. **`TransferLimitsService`**: Sets and enforces maximum daily transaction limits per account type.

