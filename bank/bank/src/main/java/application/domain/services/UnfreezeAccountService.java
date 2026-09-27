package application.domain.services;

import application.domain.models.BankAccount;

public class UnfreezeAccountService {
    public void unfreezeAccount(BankAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
    }
}
