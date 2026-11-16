package application.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import application.adapters.InMemoryAuditLogRepository;
import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryScheduledTransferRepository;
import application.adapters.InMemoryTransactionRepository;
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
    private InMemoryAuditLogRepository auditEntries;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryBankAccountRepository();
        transfers = new InMemoryScheduledTransferRepository();
        accounts.save(new BankAccount(new AccountNumber("8100000001"), Money.of("1000"),
            "COMPANY077", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("8100000002"), Money.zero(),
            "OTHER077", AccountType.SAVINGS, "COP"));
        auditEntries = new InMemoryAuditLogRepository();
        service = new ScheduleTransferService(transfers, accounts,
            new AuditService(auditEntries));
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
    @Test
    void executedScheduledTransferCreatesDebitAndCreditHistory() throws InterruptedException {
        ScheduledTransfer scheduled = service.schedule("8100000001", "8100000002",
            Money.of("125"), Instant.now().plusMillis(80), "COMPANY077",
            "operator-77", "COMPANY_OPERATIONS_EMPLOYEE");
        InMemoryTransactionRepository transactionHistory = new InMemoryTransactionRepository();
        ExecuteScheduledTransfersService executor = new ExecuteScheduledTransfersService(
            transfers, accounts, new AuditService(auditEntries), transactionHistory);

        Thread.sleep(120);
        executor.executeDue(scheduled.getId());

        assertEquals(ScheduledTransfer.Status.EXECUTED, transfers.findById(scheduled.getId()).orElseThrow().getStatus());
        assertEquals(1, transactionHistory.findByAccountId("8100000001").size());
        assertEquals(1, transactionHistory.findByAccountId("8100000002").size());
    }

}