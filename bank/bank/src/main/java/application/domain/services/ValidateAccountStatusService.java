package application.domain.services;

import application.domain.models.BankAccount;

public class ValidateAccountStatusService {
    public boolean isValid(BankAccount account) {
        return account != null;
    }
}
