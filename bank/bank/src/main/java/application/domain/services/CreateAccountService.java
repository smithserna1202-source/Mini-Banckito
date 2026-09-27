package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
public class CreateAccountService {
    private final BankAccountRepository repository;
    public CreateAccountService(BankAccountRepository repository) { this.repository = repository; }
    public void createAccount(BankAccount account) { repository.save(account); }
}
