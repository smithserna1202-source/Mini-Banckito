package application.domain.services;

import application.domain.enums.AccountStatus;
import application.domain.enums.TransactionType;
import application.domain.models.Transaction;
import application.domain.ports.TransactionRepository;
import java.util.UUID;
import application.domain.models.BankAccount;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.ScheduledTransferRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class ExecuteScheduledTransfersService {
    private final ScheduledTransferRepository transfers;
    private final BankAccountRepository accounts;
    private final AuditService audit;
    private final TransactionRepository transactions;

    public ExecuteScheduledTransfersService(ScheduledTransferRepository transfers, BankAccountRepository accounts,
                                            AuditService audit) {
        this(transfers, accounts, audit, null);
    }

    public ExecuteScheduledTransfersService(ScheduledTransferRepository transfers, BankAccountRepository accounts,
                                            AuditService audit, TransactionRepository transactions) {
        this.transfers = Objects.requireNonNull(transfers);
        this.accounts = Objects.requireNonNull(accounts);
        this.audit = Objects.requireNonNull(audit);
        this.transactions = transactions;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${bank.transfers.scheduler-delay-ms:30000}")
    public void executeAllDue() {
        for (ScheduledTransfer transfer : transfers.findAll()) {
            executeDue(transfer.getId());
        }
    }

    @Transactional
    public void executeDue(String transferId) {
        ScheduledTransfer transfer = transfers.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Scheduled transfer not found"));
        if (transfer.getStatus() != ScheduledTransfer.Status.SCHEDULED
            || transfer.getScheduledAt().isAfter(Instant.now())) return;

        BankAccount source = accounts.findByAccountNumber(transfer.getSource().getValue())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        BankAccount target = accounts.findByAccountNumber(transfer.getDestination().getValue())
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (source.getStatus() != AccountStatus.ACTIVE || target.getStatus() != AccountStatus.ACTIVE) {
            markFailed(transfer, "Both accounts must be active");
            return;
        }
        if (!source.getCurrency().equals(target.getCurrency())) {
            markFailed(transfer, "Account currencies must match");
            return;
        }

        try {
            source.withdraw(transfer.getAmount());
            try {
                target.deposit(transfer.getAmount());
            } catch (RuntimeException destinationFailure) {
                source.deposit(transfer.getAmount());
                throw destinationFailure;
            }
            accounts.save(source);
            accounts.save(target);
            if (transactions != null) {
                transactions.save(new Transaction(UUID.randomUUID().toString(),
                    transfer.getSource().getValue(), transfer.getAmount(), TransactionType.DEBIT_TRANSFER));
                transactions.save(new Transaction(UUID.randomUUID().toString(),
                    transfer.getDestination().getValue(), transfer.getAmount(), TransactionType.CREDIT_TRANSFER));
            }
            transfer.markExecuted();
            transfers.save(transfer);
            audit.record("SCHEDULED_TRANSFER_EXECUTED", transfer.getCreatorId(), "SYSTEM",
                transfer.getId(), Map.of("amount", transfer.getAmount().toString()));
        } catch (RuntimeException exception) {
            markFailed(transfer, exception.getMessage() == null ? "Execution failed" : exception.getMessage());
        }
    }

    private void markFailed(ScheduledTransfer transfer, String reason) {
        transfer.markFailed();
        transfers.save(transfer);
        audit.record("SCHEDULED_TRANSFER_FAILED", transfer.getCreatorId(), "SYSTEM", transfer.getId(),
            Map.of("reason", reason));
    }
}
