package application.domain.ports;

import application.domain.models.Transaction;
import java.util.List;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findByAccountId(String accountId);
}
