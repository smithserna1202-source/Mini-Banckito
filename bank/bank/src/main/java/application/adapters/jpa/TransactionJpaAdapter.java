package application.adapters.jpa;
import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import application.domain.valueobjects.Money;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class TransactionJpaAdapter implements TransactionRepository {
 private final SpringDataTransactionRepository repository;
 public TransactionJpaAdapter(SpringDataTransactionRepository repository){this.repository=repository;}
 @Override public void save(Transaction transaction){repository.save(new TransactionEntity(transaction));}
 @Override public List<Transaction> findByAccountId(String accountId){
  return repository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
   .map(e->new Transaction(e.getId(),e.getAccountId(),new Money(e.getAmount()),e.getType())).toList();
 }
}
