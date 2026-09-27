package application.adapters;

import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        return transactions;
    }
}
