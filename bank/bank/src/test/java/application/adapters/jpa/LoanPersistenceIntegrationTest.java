package application.adapters.jpa;

import static org.junit.jupiter.api.Assertions.*;

import application.domain.enums.LoanStatus;
import application.domain.models.Loan;
import application.domain.ports.LoanRepository;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "BANK_DEFAULT_PASSWORD=Integration-Only-Password-123",
    "spring.datasource.url=jdbc:h2:mem:loan-persistence-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class LoanPersistenceIntegrationTest {
    @Autowired private LoanRepository loans;

    @Test
    void approvedLoanRetainsItsStateAndTimestampsAfterReload() {
        Loan original = new Loan("loan-persistence-test", "C001", "PERSONAL",
            Money.of("250000.00"), 12, "1000000001");
        original.approve("analyst01", Money.of("200000.00"), new BigDecimal("1.2500"));
        loans.save(original);

        Loan restored = loans.findById(original.getId()).orElseThrow();
        assertEquals(LoanStatus.APPROVED, restored.getStatus());
        assertEquals(original.getApprovedAmount(), restored.getApprovedAmount());
        assertEquals(original.getInterestRate(), restored.getInterestRate());
        assertEquals(original.getRequestedAt().truncatedTo(ChronoUnit.MILLIS),
            restored.getRequestedAt().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(original.getApprovedAt().truncatedTo(ChronoUnit.MILLIS),
            restored.getApprovedAt().truncatedTo(ChronoUnit.MILLIS));
        assertNull(restored.getDisbursedAt());
    }
}
