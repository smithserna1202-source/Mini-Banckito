package application.domain.services;
import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import java.util.List;
import java.util.Objects;
public class GetTransactionHistoryService {
 private final TransactionRepository repository;
 public GetTransactionHistoryService(TransactionRepository repository){this.repository=Objects.requireNonNull(repository);}
 public List<Transaction> getHistory(String accountId){return repository.findByAccountId(accountId);}
}