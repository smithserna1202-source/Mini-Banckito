package application.domain.services;

import application.domain.valueobjects.AccountNumber;

public class GenerateAccountStatementService {
    public void generateStatement(AccountNumber accountNumber, String month) {
        if (accountNumber == null || month == null) {
            throw new IllegalArgumentException("Account number and month are required");
        }
    }
}
