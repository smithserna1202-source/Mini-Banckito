# Value Objects Specification

Value Objects represent immutable attributes within the domain without a distinct identity.

## Defined Value Objects:
1. **`Money`**: Encapsulates monetary amounts, preventing negative values and currency operations.
2. **`AccountNumber`**: Ensures valid bank account number formatting.
3. **`Email`**: Validates corporate or personal email formatting (must contain `@` and domain).
4. **`PhoneNumber`**: Validates phone numbers with length rules (7 to 15 digits).

