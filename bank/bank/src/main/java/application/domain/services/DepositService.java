package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class DepositService {
    public void deposit(BankAccount account, Money amount) {
        if (account == null || amount == null) {
            throw new IllegalArgumentException("Account and amount cannot be null");
        }
        account.deposit(amount);
    }
}
