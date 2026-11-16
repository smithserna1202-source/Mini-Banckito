package application.domain.ports;

import application.domain.enums.TransactionType;
import application.domain.models.Transaction;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findByAccountId(String accountId);

    default Money sumOutgoingTransfersSince(String accountId, Instant since) {
        return findByAccountId(accountId).stream()
            .filter(t -> t.getType() == TransactionType.DEBIT_TRANSFER)
            .filter(t -> !t.getCreatedAt().isBefore(since))
            .map(Transaction::getAmount)
            .reduce(Money.zero(), Money::add);
    }
}
