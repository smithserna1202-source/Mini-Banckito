package application.adapters.jpa;

import static org.junit.jupiter.api.Assertions.*;

import application.domain.enums.AccountStatus;
import application.domain.enums.AccountType;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
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
    "spring.datasource.url=jdbc:h2:mem:account-persistence-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class BankAccountPersistenceIntegrationTest {
    @Autowired private BankAccountRepository accounts;

    @Test
    void accountStatusAndOpeningDateSurviveRepositoryReload() {
        BankAccount original = new BankAccount(new AccountNumber("3000000001"),
            Money.of("15000.00"), "C001", AccountType.SAVINGS, "COP");
        original.freeze();
        accounts.save(original);

        BankAccount restored = accounts.findByAccountNumber("3000000001").orElseThrow();
        assertEquals(AccountStatus.FROZEN, restored.getStatus());
        assertEquals(original.getBalance(), restored.getBalance());
        assertEquals(original.getOwnerId(), restored.getOwnerId());
        assertEquals(original.getType(), restored.getType());
        assertEquals(original.getCurrency(), restored.getCurrency());
        assertTrue(Duration.between(original.getOpenedAt(), restored.getOpenedAt()).abs()
            .compareTo(Duration.ofMillis(1)) <= 0);
        assertThrows(IllegalStateException.class, () -> restored.withdraw(Money.of("1.00")));
    }
}
