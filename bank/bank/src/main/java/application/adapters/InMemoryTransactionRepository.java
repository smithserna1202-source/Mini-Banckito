package application.adapters;
import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
public class InMemoryTransactionRepository implements TransactionRepository {
 private final List<Transaction> transactions=new CopyOnWriteArrayList<>();
 @Override public void save(Transaction transaction){transactions.add(transaction);}
 @Override public List<Transaction> findByAccountId(String accountId){
  return transactions.stream().filter(t->t.getAccountId().equals(accountId)).collect(Collectors.toUnmodifiableList());
 }
}