package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class InterbankTransferService {
    public void transferToExternalBank(BankAccount source, String bankCode, String externalAccount, Money amount) {
        source.withdraw(amount);
    }
}
