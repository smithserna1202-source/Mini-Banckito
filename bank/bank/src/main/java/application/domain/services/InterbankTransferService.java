package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

/**
 * Safe placeholder until a real external clearing provider is configured.
 * Never debit the customer's account without a confirmed external settlement.
 */
public class InterbankTransferService {
    public void transferToExternalBank(BankAccount source, String bankCode, String externalAccount, Money amount) {
        if (source == null || bankCode == null || bankCode.isBlank()
                || externalAccount == null || externalAccount.isBlank()
                || amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("A source account, bank, destination and positive amount are required");
        }
        throw new UnsupportedOperationException(
            "External-bank transfers are disabled until a regulated payment/clearing provider is integrated");
    }
}
