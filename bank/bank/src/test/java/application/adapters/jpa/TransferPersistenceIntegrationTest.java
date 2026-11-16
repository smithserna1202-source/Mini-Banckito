package application.adapters.jpa;

import static org.junit.jupiter.api.Assertions.*;

import application.domain.enums.TransferStatus;
import application.domain.models.BankTransfer;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.BankTransferRepository;
import application.domain.ports.ScheduledTransferRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "BANK_DEFAULT_PASSWORD=Integration-Only-Password-123",
    "spring.datasource.url=jdbc:h2:mem:transfer-persistence-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class TransferPersistenceIntegrationTest {
    @Autowired private BankTransferRepository bankTransfers;
    @Autowired private ScheduledTransferRepository scheduledTransfers;

    @Test
    void companyTransferSurvivesRepositoryReload() {
        BankTransfer original = new BankTransfer("company-transfer-test", "1000000001",
            "1000000002", Money.of("125000.50"), "operator01", true);
        original.approve("supervisor01");
        bankTransfers.save(original);

        BankTransfer restored = bankTransfers.findById(original.getId()).orElseThrow();
        assertEquals(TransferStatus.APPROVED, restored.getStatus());
        assertEquals(original.getAmount(), restored.getAmount());
        assertEquals("supervisor01", restored.getApproverUserId());
        assertInstantsClose(original.getCreatedAt(), restored.getCreatedAt());
    }

    @Test
    void scheduledTransferSurvivesRepositoryReload() {
        Instant scheduledAt = Instant.now().plusSeconds(600);
        ScheduledTransfer original = new ScheduledTransfer("scheduled-transfer-test",
            new AccountNumber("2000000001"), new AccountNumber("2000000002"),
            Money.of("8500.25"), scheduledAt, "customer01");
        scheduledTransfers.save(original);

        ScheduledTransfer restored = scheduledTransfers.findById(original.getId()).orElseThrow();
        assertEquals(ScheduledTransfer.Status.SCHEDULED, restored.getStatus());
        assertEquals(original.getAmount(), restored.getAmount());
        assertInstantsClose(original.getScheduledAt(), restored.getScheduledAt());
        assertInstantsClose(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getCreatorId(), restored.getCreatorId());
    }

    private static void assertInstantsClose(Instant expected, Instant actual) {
        assertTrue(Duration.between(expected, actual).abs().compareTo(Duration.ofMillis(1)) <= 0,
            () -> "Expected timestamps within 1 ms, but were " + expected + " and " + actual);
    }
}
