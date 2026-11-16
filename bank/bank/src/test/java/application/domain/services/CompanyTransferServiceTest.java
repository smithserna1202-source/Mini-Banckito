package application.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import application.adapters.InMemoryAuditLogRepository;
import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryBankTransferRepository;
import application.domain.enums.AccountType;
import application.domain.enums.Role;
import application.domain.enums.TransferStatus;
import application.domain.enums.TransactionType;
import application.adapters.InMemoryTransactionRepository;
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
    private InMemoryTransactionRepository transactionEntries;
    private CompanyTransferService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryBankAccountRepository();
        transfers = new InMemoryBankTransferRepository();
        auditEntries = new InMemoryAuditLogRepository();
        transactionEntries = new InMemoryTransactionRepository();
        accounts.save(new BankAccount(new AccountNumber("4000000001"), Money.of("10000000.00"),
            "COMPANY001", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("4000000002"), Money.of("0.00"),
            "OTHER001", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("4000000003"), Money.of("10000000.00"),
            "COMPANY002", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("4000000004"), Money.of("0.00"),
            "OTHER002", AccountType.CHECKING, "COP"));
        service = new CompanyTransferService(accounts, transfers, new AuthorizationService(),
            new AuditService(auditEntries), Money.of("5000000.00"), transactionEntries);
    }

    @Test
    void lowValueTransferExecutesImmediatelyAndWritesAudit() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("100000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(TransferStatus.EXECUTED, transfer.getStatus());
        assertEquals(Money.of("9900000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("100000.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());
        assertEquals(1, auditEntries.findAll().size());
        assertEquals(1, transactionEntries.findByAccountId("4000000001").size());
        assertEquals(TransactionType.DEBIT_TRANSFER, transactionEntries.findByAccountId("4000000001").get(0).getType());
        assertEquals(TransactionType.CREDIT_TRANSFER, transactionEntries.findByAccountId("4000000002").get(0).getType());
    }

    @Test
    void highValueTransferWaitsForSupervisorBeforeMovingFunds() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("6000000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(TransferStatus.PENDING_APPROVAL, transfer.getStatus());
        assertEquals(Money.of("10000000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("0.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());

        service.approve(transfer.getId(), "supervisor01", Role.COMPANY_SUPERVISOR, "COMPANY001");

        assertEquals(TransferStatus.EXECUTED, transfers.findById(transfer.getId()).orElseThrow().getStatus());
        assertEquals(Money.of("4000000.00"), accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("6000000.00"), accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());
        assertEquals(3, auditEntries.findAll().size());
        assertEquals(TransactionType.DEBIT_TRANSFER, transactionEntries.findByAccountId("4000000001").get(0).getType());
        assertEquals(TransactionType.CREDIT_TRANSFER, transactionEntries.findByAccountId("4000000002").get(0).getType());
    }

    @Test
    void tellerCannotCreateCompanyTransfer() {
        assertThrows(SecurityException.class, () -> service.create("COMPANY001",
            "4000000001", "4000000002", Money.of("100.00"), "teller01", Role.TELLER));
    }
    @Test
    void supervisorCannotApproveAnotherCompanyTransfer() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("6000000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertThrows(SecurityException.class, () -> service.approve(transfer.getId(),
            "supervisor-from-other-company", Role.COMPANY_SUPERVISOR, "COMPANY002"));
        assertEquals(TransferStatus.PENDING_APPROVAL,
            transfers.findById(transfer.getId()).orElseThrow().getStatus());
        assertEquals(Money.of("10000000.00"),
            accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
    }

    @Test
    void approvalRejectsTransferWhenFundsAreInsufficient() {
        BankTransfer transfer = service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("11000000.00"), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertThrows(IllegalStateException.class, () -> service.approve(transfer.getId(),
            "supervisor01", Role.COMPANY_SUPERVISOR, "COMPANY001"));
        assertEquals(TransferStatus.PENDING_APPROVAL,
            transfers.findById(transfer.getId()).orElseThrow().getStatus());
        assertEquals(Money.of("10000000.00"),
            accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
    }

    @Test
    void pendingQueueIsFilteredByCompanyOwnership() {
        service.create("COMPANY001", "4000000001", "4000000002",
            Money.of("6000000.00"), "operator-1", Role.COMPANY_OPERATIONS_EMPLOYEE);
        service.create("COMPANY002", "4000000003", "4000000004",
            Money.of("7000000.00"), "operator-2", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(1, service.findPendingForCompany("COMPANY001").size());
        assertEquals(1, service.findPendingForCompany("COMPANY002").size());
        assertEquals(2, service.findPendingForCompany(null).size());
    }

    @Test
    void batchPaymentsExecuteAllValidatedLowValueTransfers() {
        var created = service.createBatch("COMPANY001", "4000000001", java.util.List.of(
            new CompanyTransferService.PaymentInstruction("4000000002", Money.of("100000.00")),
            new CompanyTransferService.PaymentInstruction("4000000002", Money.of("200000.00"))
        ), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE);

        assertEquals(2, created.size());
        assertTrue(created.stream().allMatch(t -> t.getStatus() == TransferStatus.EXECUTED));
        assertEquals(Money.of("9700000.00"),
            accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
        assertEquals(Money.of("300000.00"),
            accounts.findByAccountNumber("4000000002").orElseThrow().getBalance());
    }

    @Test
    void batchPaymentsAreRejectedBeforeExecutionWhenImmediateTotalExceedsBalance() {
        assertThrows(IllegalStateException.class, () -> service.createBatch("COMPANY001",
            "4000000001", java.util.List.of(
                new CompanyTransferService.PaymentInstruction("4000000002", Money.of("4000000.00")),
                new CompanyTransferService.PaymentInstruction("4000000002", Money.of("4000000.00")),
                new CompanyTransferService.PaymentInstruction("4000000002", Money.of("4000000.00"))
            ), "company-operator", Role.COMPANY_OPERATIONS_EMPLOYEE));

        assertTrue(transfers.findAll().isEmpty());
        assertEquals(Money.of("10000000.00"),
            accounts.findByAccountNumber("4000000001").orElseThrow().getBalance());
    }

}