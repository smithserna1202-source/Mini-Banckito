package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import java.util.Objects;
public class CreateAccountService {
 private final BankAccountRepository repository;
 public CreateAccountService(BankAccountRepository repository){this.repository=Objects.requireNonNull(repository);}
 public void createAccount(BankAccount account){
  Objects.requireNonNull(account,"Account is required");
  if(repository.findByAccountNumber(account.getAccountNumber().getValue()).isPresent())throw new IllegalArgumentException("Account number already exists");
  repository.save(account);
 }
}