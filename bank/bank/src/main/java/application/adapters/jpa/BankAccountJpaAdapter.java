package application.adapters.jpa;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class BankAccountJpaAdapter implements BankAccountRepository {
 private final SpringDataBankAccountRepository repository;
 public BankAccountJpaAdapter(SpringDataBankAccountRepository repository){this.repository=repository;}
 @Override public void save(BankAccount account){repository.save(new BankAccountEntity(account));}
 @Override public Optional<BankAccount> findByAccountNumber(String number){
  return repository.findById(number).map(e->{
   BankAccount a=new BankAccount(new AccountNumber(e.getAccountNumber()),new Money(e.getBalance()),e.getOwnerId(),e.getType(),e.getCurrency());
   if(e.getStatus()==application.domain.enums.AccountStatus.FROZEN)a.freeze();
   else if(e.getStatus()==application.domain.enums.AccountStatus.BLOCKED)a.block();
   else if(e.getStatus()==application.domain.enums.AccountStatus.CLOSED)a.close();
   else if(e.getStatus()==application.domain.enums.AccountStatus.CANCELLED)a.cancel();
   return a;
  });
 }
}