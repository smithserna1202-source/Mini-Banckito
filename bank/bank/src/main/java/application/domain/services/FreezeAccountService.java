package application.domain.services;

import application.domain.models.BankAccount;

public class FreezeAccountService {
    public void freezeAccount(BankAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
    }
}
