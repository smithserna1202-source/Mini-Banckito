# Domain Models Specification

## Entities

### 1. BankAccount
* **Attributes**: `accountNumber` (AccountNumber), `balance` (Money), `ownerId` (String).
* **Behaviors**: `deposit(Money)`, `withdraw(Money)`.

### 2. Customer
* **Attributes**: `id` (String), `fullName` (String), `email` (Email).
* **Behaviors**: Update contact information, represent Natural Person customer.

