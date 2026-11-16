package application.adapters.jpa;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.util.Optional;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class BankAccountJpaAdapter implements BankAccountRepository {
 private final SpringDataBankAccountRepository repository;
 public BankAccountJpaAdapter(SpringDataBankAccountRepository repository){this.repository=repository;}
 @Override public void save(BankAccount account){repository.save(new BankAccountEntity(account));}
 @Override public Optional<BankAccount> findByAccountNumber(String number){return repository.findById(number).map(this::toDomain);}
 @Override public List<BankAccount> findAll(){return repository.findAll().stream().map(this::toDomain).toList();}
 private BankAccount toDomain(BankAccountEntity e){return BankAccount.restore(new AccountNumber(e.getAccountNumber()),new Money(e.getBalance()),e.getOwnerId(),e.getType(),e.getCurrency(),e.getStatus(),e.getOpenedAt());}
}
