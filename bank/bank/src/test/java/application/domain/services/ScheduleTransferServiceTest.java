package application.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import application.adapters.InMemoryAuditLogRepository;
import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryScheduledTransferRepository;
import application.domain.enums.AccountType;
import application.domain.models.BankAccount;
import application.domain.models.ScheduledTransfer;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleTransferServiceTest {
    private InMemoryBankAccountRepository accounts;
    private InMemoryScheduledTransferRepository transfers;
    private ScheduleTransferService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryBankAccountRepository();
        transfers = new InMemoryScheduledTransferRepository();
        accounts.save(new BankAccount(new AccountNumber("8100000001"), Money.of("1000"),
            "COMPANY077", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("8100000002"), Money.zero(),
            "OTHER077", AccountType.SAVINGS, "COP"));
        service = new ScheduleTransferService(transfers, accounts,
            new AuditService(new InMemoryAuditLogRepository()));
    }

    @Test
    void companyEmployeeCanScheduleForCompanyWithoutImpersonatingCompanyId() {
        ScheduledTransfer scheduled = service.schedule("8100000001", "8100000002",
            Money.of("100"), Instant.now().plusSeconds(120), "COMPANY077",
            "operator-77", "COMPANY_OPERATIONS_EMPLOYEE");

        assertEquals("operator-77", scheduled.getCreatorId());
        assertEquals(ScheduledTransfer.Status.SCHEDULED, scheduled.getStatus());
    }

    @Test
    void employeeCannotScheduleFromAnotherCompanyAccount() {
        assertThrows(SecurityException.class, () -> service.schedule("8100000001", "8100000002",
            Money.of("100"), Instant.now().plusSeconds(120), "COMPANY099",
            "operator-99", "COMPANY_OPERATIONS_EMPLOYEE"));
        assertTrue(transfers.findAll().isEmpty());
    }
}
