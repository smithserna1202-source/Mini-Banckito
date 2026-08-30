# Domain Model - Mini-Banckito

## Overview

This document describes the business domain for the Mini-Banckito application, detailing the main entities, value objects, and business rules.

---

## Core Entities

### 1. BankAccount
Represents a bank account managed by the system.

- **Attributes:**
  - `accountNumber`: Unique identifier.
  - `balance`: Current amount of money available.
  - `owner`: Customer who owns the account.

- **Business Rules:**
  - Balance cannot be negative.
  - Deposits must be greater than zero.
  - Withdrawals cannot exceed the current balance.

### 2. Customer
Represents the user of the banking system.

- **Attributes:**
  - `id`: Identification number.
  - `name`: Full name of the customer.
  - `email`: Contact email address.

---

## Domain Services

- **TransferService:** Manages money transfers between two `BankAccount` instances, validating sufficient funds before executing the operation.
