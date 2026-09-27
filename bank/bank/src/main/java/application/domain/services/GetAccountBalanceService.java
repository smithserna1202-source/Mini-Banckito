package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class GetAccountBalanceService {
    public Money checkBalance(BankAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        return account.getBalance();
    }
}
