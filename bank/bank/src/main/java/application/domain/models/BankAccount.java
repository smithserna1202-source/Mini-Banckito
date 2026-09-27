package application.domain.models;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;

public class BankAccount {
    private final AccountNumber accountNumber;
    private Money balance;

    public BankAccount(AccountNumber accountNumber, Money initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public AccountNumber getAccountNumber() { return accountNumber; }
    public Money getBalance() { return balance; }

    public void deposit(Money amount) {
        this.balance = new Money(this.balance.getAmount() + amount.getAmount());
    }

    public void withdraw(Money amount) {
        if (amount.getAmount() > this.balance.getAmount()) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance = new Money(this.balance.getAmount() - amount.getAmount());
    }
}
