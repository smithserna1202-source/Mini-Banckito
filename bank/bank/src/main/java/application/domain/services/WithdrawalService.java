package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class WithdrawalService {
    public void withdraw(BankAccount account, Money amount) {
        if (account == null || amount == null) {
            throw new IllegalArgumentException("Account and amount cannot be null");
        }
        account.withdraw(amount);
    }
}
