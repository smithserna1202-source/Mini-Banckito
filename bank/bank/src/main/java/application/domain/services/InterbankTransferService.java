package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class InterbankTransferService {
    public void transferToExternalBank(BankAccount source, String bankCode, String externalAccount, Money amount) {
        if (source == null || bankCode == null || externalAccount == null || amount == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
        source.withdraw(amount);
    }
}
