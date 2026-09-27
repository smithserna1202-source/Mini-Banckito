package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class TransferService {
    public void transfer(BankAccount source, BankAccount destination, Money amount) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination accounts must not be null");
        }
        source.withdraw(amount);
        destination.deposit(amount);
    }
}
