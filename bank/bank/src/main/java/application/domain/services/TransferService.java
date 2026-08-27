package application.domain.services;

import application.domain.models.BankAccount;

public interface TransferService {
    void transfer(BankAccount fromAccount, BankAccount toAccount, double amount);
}
