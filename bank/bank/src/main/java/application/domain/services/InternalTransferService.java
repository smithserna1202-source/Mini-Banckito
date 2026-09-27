package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class InternalTransferService {
    public void transfer(BankAccount source, BankAccount target, Money amount) {
        if (source == null || target == null || amount == null) {
            throw new IllegalArgumentException("Source, target and amount cannot be null");
        }
        source.withdraw(amount);
        target.deposit(amount);
    }
}
