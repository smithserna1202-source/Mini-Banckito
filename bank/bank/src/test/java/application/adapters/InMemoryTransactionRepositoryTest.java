package application.adapters;

import application.domain.enums.TransactionType;
import application.domain.models.Transaction;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {
 @Test void historyReturnsOnlyTransactionsForRequestedAccount(){
  InMemoryTransactionRepository repository=new InMemoryTransactionRepository();
  repository.save(new Transaction("T1","ACC-1",Money.of("10"),TransactionType.DEPOSIT));
  repository.save(new Transaction("T2","ACC-2",Money.of("20"),TransactionType.WITHDRAWAL));
  var history=repository.findByAccountId("ACC-1");
  assertEquals(1,history.size());
  assertEquals("T1",history.get(0).getId());
 }
 @Test void historyIsReadOnly(){
  InMemoryTransactionRepository repository=new InMemoryTransactionRepository();
  repository.save(new Transaction("T1","ACC-1",Money.of("10"),TransactionType.DEPOSIT));
  assertThrows(UnsupportedOperationException.class,()->repository.findByAccountId("ACC-1").clear());
 }
}
