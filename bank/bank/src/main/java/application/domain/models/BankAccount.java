package application.domain.models;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;

public class BankAccount {
    private final AccountNumber accountNumber;
    private Money balance;
    private final String ownerId;

    public BankAccount(AccountNumber accountNumber, Money balance, String ownerId) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.ownerId = ownerId;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public Money getBalance() {
        return balance;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount);
    }
}
