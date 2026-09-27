package application.domain.services;

import application.domain.models.BankAccount;

public class CloseAccountService {
    public void closeAccount(BankAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getBalance().getAmount() > 0) {
            throw new IllegalStateException("Cannot close an account with a positive balance");
        }
    }
}
