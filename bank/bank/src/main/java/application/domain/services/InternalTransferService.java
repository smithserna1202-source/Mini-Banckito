package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class InternalTransferService {
    public void transfer(BankAccount source, BankAccount target, Money amount) {
        source.withdraw(amount);
        target.deposit(amount);
    }
}
