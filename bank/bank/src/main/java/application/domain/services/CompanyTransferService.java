package application.domain.services;

import application.domain.enums.AccountStatus;
import application.domain.enums.Role;
import application.domain.enums.TransferStatus;
import application.domain.models.BankAccount;
import application.domain.models.BankTransfer;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.BankTransferRepository;
import application.domain.ports.TransactionRepository;
import application.domain.models.Transaction;
import application.domain.enums.TransactionType;
import application.domain.valueobjects.Money;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class CompanyTransferService {
    private final BankAccountRepository accounts;
    private final BankTransferRepository transfers;
    private final AuthorizationService authorization;
    private final AuditService audit;
    private final Money approvalThreshold;
    private final TransactionRepository transactions;

    public CompanyTransferService(BankAccountRepository accounts, BankTransferRepository transfers,
                                  AuthorizationService authorization, AuditService audit,
                                  Money approvalThreshold) {
        this(accounts, transfers, authorization, audit, approvalThreshold, null);
    }

    public CompanyTransferService(BankAccountRepository accounts, BankTransferRepository transfers,
                                  AuthorizationService authorization, AuditService audit,
                                  Money approvalThreshold, TransactionRepository transactions) {
        this.accounts = Objects.requireNonNull(accounts);
        this.transfers = Objects.requireNonNull(transfers);
        this.authorization = Objects.requireNonNull(authorization);
        this.audit = Objects.requireNonNull(audit);
        if (approvalThreshold == null || !approvalThreshold.isPositive()) {
            throw new IllegalArgumentException("Approval threshold must be positive");
        }
        this.approvalThreshold = approvalThreshold;
        this.transactions = transactions;
    }

    @Transactional
    public BankTransfer create(String companyId, String sourceId, String destinationId,
                               Money amount, String userId, Role role) {
        authorization.require(role, AuthorizationService.Action.REQUEST_TRANSFER);
        BankAccount source = accounts.findByAccountNumber(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        BankAccount target = accounts.findByAccountNumber(destinationId)
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        if (!source.getOwnerId().equals(companyId)) {
            throw new SecurityException("Employee cannot operate on another company account");
        }
        if (source.getStatus() != AccountStatus.ACTIVE || target.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Both accounts must be active");
        }
        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new IllegalArgumentException("Account currencies must match");
        }

        boolean approval = amount.isGreaterThan(approvalThreshold);
        BankTransfer transfer = new BankTransfer(UUID.randomUUID().toString(), sourceId,
            destinationId, amount, userId, approval);
        transfers.save(transfer);
        if (!approval) {
            execute(transfer);
        } else {
            audit.record("TRANSFER_PENDING_APPROVAL", userId, role.name(), transfer.getId(),
                Map.of("amount", amount.toString()));
        }
        return transfer;
    }

    @Transactional
    public void approve(String id, String supervisorId, Role role) {
        authorization.require(role, AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
        BankTransfer transfer = require(id);
        transfer.expire();
        if (transfer.getStatus() == TransferStatus.EXPIRED) {
            transfers.save(transfer);
            audit.record("TRANSFER_EXPIRED", supervisorId, role.name(), id, Map.of());
            return;
        }
        transfer.approve(supervisorId);
        transfers.save(transfer);
        execute(transfer);
    }

    @Transactional
    public void reject(String id, String supervisorId, Role role) {
        authorization.require(role, AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
        BankTransfer transfer = require(id);
        transfer.expire();
        if (transfer.getStatus() == TransferStatus.EXPIRED) {
            transfers.save(transfer);
            audit.record("TRANSFER_EXPIRED", supervisorId, role.name(), id, Map.of());
            return;
        }
        transfer.reject(supervisorId);
        transfers.save(transfer);
        audit.record("TRANSFER_REJECTED", supervisorId, role.name(), id, Map.of());
    }

    @Transactional
    @Scheduled(fixedDelayString = "${bank.transfers.expiration-delay-ms:60000}")
    public void expirePending() {
        transfers.findAll().forEach(transfer -> {
            TransferStatus before = transfer.getStatus();
            transfer.expire();
            if (before != transfer.getStatus()) {
                transfers.save(transfer);
                audit.record("TRANSFER_EXPIRED", "SYSTEM", "SYSTEM", transfer.getId(), Map.of());
            }
        });
    }

    private BankTransfer require(String id) {
        return transfers.findById(id).orElseThrow(() -> new IllegalArgumentException("Transfer not found"));
    }

    private void execute(BankTransfer transfer) {
        BankAccount source = accounts.findByAccountNumber(transfer.getSourceAccount())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        BankAccount target = accounts.findByAccountNumber(transfer.getDestinationAccount())
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        Money sourceBefore = source.getBalance();
        Money targetBefore = target.getBalance();
        source.withdraw(transfer.getAmount());
        try {
            target.deposit(transfer.getAmount());
        } catch (RuntimeException exception) {
            source.deposit(transfer.getAmount());
            throw exception;
        }
        transfer.execute();
        accounts.save(source);
        accounts.save(target);
        transfers.save(transfer);
        if (transactions != null) {
            transactions.save(new Transaction(UUID.randomUUID().toString(), transfer.getSourceAccount(),
                transfer.getAmount(), TransactionType.DEBIT_TRANSFER));
            transactions.save(new Transaction(UUID.randomUUID().toString(), transfer.getDestinationAccount(),
                transfer.getAmount(), TransactionType.CREDIT_TRANSFER));
        }
        audit.record("TRANSFER_EXECUTED", transfer.getCreatorUserId(), "COMPANY", transfer.getId(),
            Map.of("amount", transfer.getAmount().toString(),
                "source", transfer.getSourceAccount(), "sourceBalanceBefore", sourceBefore.toString(),
                "sourceBalanceAfter", source.getBalance().toString(),
                "destination", transfer.getDestinationAccount(), "destinationBalanceBefore", targetBefore.toString(),
                "destinationBalanceAfter", target.getBalance().toString()));
    }
}
