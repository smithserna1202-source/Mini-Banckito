package application.domain.services;

import application.domain.valueobjects.AccountNumber;

public class GetTransactionHistoryService {
    public void getHistory(AccountNumber accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null");
        }
    }
}
