package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;

public class AccountManagementService {
    private final BankAccountRepository repository;

    public AccountManagementService(BankAccountRepository repository) {
        this.repository = repository;
    }

    public void createAccount(BankAccount account) {
        repository.save(account);
    }

    public void deposit(AccountNumber accountNumber, Money amount) {
        BankAccount account = repository.findByAccountNumber(accountNumber.getValue())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.deposit(amount);
        repository.save(account);
    }

    public void withdraw(AccountNumber accountNumber, Money amount) {
        BankAccount account = repository.findByAccountNumber(accountNumber.getValue())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.withdraw(amount);
        repository.save(account);
    }
}
