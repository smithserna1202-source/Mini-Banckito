package application.adapters.jpa;

import static org.junit.jupiter.api.Assertions.*;

import application.domain.enums.TransactionType;
import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import application.domain.valueobjects.Money;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "BANK_DEFAULT_PASSWORD=Integration-Only-Password-123",
    "spring.datasource.url=jdbc:h2:mem:transaction-ledger-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class TransactionLedgerIntegrationTest {
    @Autowired private TransactionRepository transactions;

    @Test
    void debitAndCreditEntriesPersistAndOnlyDebitsCountAsOutgoing() {
        Instant now = Instant.now();
        transactions.save(new Transaction("ledger-debit-1", "5000000001",
            Money.of("125.50"), TransactionType.DEBIT_TRANSFER, now));
        transactions.save(new Transaction("ledger-credit-1", "5000000002",
            Money.of("125.50"), TransactionType.CREDIT_TRANSFER, now));
        transactions.save(new Transaction("ledger-old-debit", "5000000001",
            Money.of("75.00"), TransactionType.DEBIT_TRANSFER, now.minusSeconds(86400)));

        assertEquals(2, transactions.findByAccountId("5000000001").size());
        assertEquals(Money.of("125.50"),
            transactions.sumOutgoingTransfersSince("5000000001", now.minusSeconds(60)));
    }
}
