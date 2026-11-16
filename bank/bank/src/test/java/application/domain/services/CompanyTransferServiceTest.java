package application.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import application.adapters.InMemoryAuditLogRepository;
import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryBankTransferRepository;
import application.domain.enums.AccountType;
import application.domain.enums.Role;
import application.domain.enums.TransferStatus;
import application.domain.models.BankAccount;
import application.domain.models.BankTransfer;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompanyTransferServiceTest {
    private InMemoryBankAccountRepository accounts;
    private InMemoryBankTransferRepository transfers;
    private InMemoryAuditLogRepository auditEntries;
    private CompanyTransferService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryBankAccountRepository();
        transfers = new InMemoryBankTransferRepository();
        auditEntries = new InMemoryAuditLogRepository();
        accounts.save(new BankAccount(new AccountNumber("4000000001"), Money.of("10000000.00"),
            "COMPANY001", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("4000000002"), Money.of("0.00"),
            "OTHER001", AccountType.CHECKING, "COP"));
        service = new CompanyTransferService(accounts, transfers, new AuthorizationService(),
            new AuditService(auditEntries), Money.of("5000000.00"));
    }

    @Test
    void lowValueTransferExecutesImmediatelyAndWritesAudit() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("100000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(TransferStatus.EXECUTED, transfer.getStatus());
        assertEquals(Money.of("9900000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("100000.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());
        assertEquals(1, auditEntries.findAll().size());
    }

    @Test
    void highValueTransferWaitsForSupervisorBeforeMovingFunds() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("6000000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(TransferStatus.PENDING_APPROVAL, transfer.getStatus());
        assertEquals(Money.of("10000000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("0.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());

        service.approve(transfer.getId(), "supervisor01", Role.COMPANY_SUPERVISOR);

        assertEquals(TransferStatus.EXECUTED, transfers.findById(transfer.getId()).orElseThrow().getStatus());
        assertEquals(Money.of("4000000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("6000000.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());
        assertEquals(2, auditEntries.findAll().size());
    }

    @Test
    void tellerCannotCreateCompanyTransfer() {
        assertThrows(SecurityException.class, () -> service.create("COMPANY001",
            "4000000001", "4000000002", Money.of("100.00"), "teller01", Role.TELLER));
    }
}
