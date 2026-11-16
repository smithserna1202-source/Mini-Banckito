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
import java.util.List;
import java.math.BigDecimal;
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


    public java.util.List<BankTransfer> findPendingForCompany(String companyId) {
        return transfers.findAll().stream()
            .filter(transfer -> transfer.getStatus() == TransferStatus.PENDING_APPROVAL)
            .filter(transfer -> {
                if (companyId == null) return true;
                BankAccount source = accounts.findByAccountNumber(transfer.getSourceAccount()).orElse(null);
                return source != null && companyId.equals(source.getOwnerId());
            })
            .toList();
    }

    public record PaymentInstruction(String destinationAccount, Money amount) {
        public PaymentInstruction {
            if (destinationAccount == null || destinationAccount.isBlank()
                    || amount == null || !amount.isPositive()) {
                throw new IllegalArgumentException("Each batch payment requires a destination and positive amount");
            }
        }
    }

    @Transactional
    public List<BankTransfer> createBatch(String companyId, String sourceId,
            List<PaymentInstruction> payments, String userId, Role role) {
        if (payments == null || payments.isEmpty() || payments.size() > 100) {
            throw new IllegalArgumentException("A batch must contain between 1 and 100 payments");
        }
        authorization.require(role, AuthorizationService.Action.REQUEST_TRANSFER);
        BankAccount source = accounts.findByAccountNumber(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        if (role != Role.ADMINISTRATOR && !source.getOwnerId().equals(companyId)) {
            throw new SecurityException("Employee cannot operate on another company account");
        }
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }

        BigDecimal immediateTotal = BigDecimal.ZERO;
        for (PaymentInstruction payment : payments) {
            BankAccount destination = accounts.findByAccountNumber(payment.destinationAccount())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
            if (destination.getStatus() != AccountStatus.ACTIVE
                    || !source.getCurrency().equals(destination.getCurrency())) {
                throw new IllegalStateException("All destination accounts must be active and use the source currency");
            }
            if (payment.destinationAccount().equals(sourceId)) {
                throw new IllegalArgumentException("Source and destination accounts must differ");
            }
            if (!payment.amount().isGreaterThan(approvalThreshold)) {
                immediateTotal = immediateTotal.add(payment.amount().getAmountDecimal());
            }
        }
        if (immediateTotal.compareTo(source.getBalance().getAmountDecimal()) > 0) {
            throw new IllegalStateException("Insufficient funds for immediate payments in batch");
        }

        return payments.stream().map(payment -> create(companyId, sourceId,
            payment.destinationAccount(), payment.amount(), userId, role)).toList();
    }

    @Transactional
    public BankTransfer create(String companyId, String sourceId, String destinationId,
                               Money amount, String userId, Role role) {
        authorization.require(role, AuthorizationService.Action.REQUEST_TRANSFER);
        BankAccount source = accounts.findByAccountNumber(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        BankAccount target = accounts.findByAccountNumber(destinationId)
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        if (role != Role.ADMINISTRATOR && !source.getOwnerId().equals(companyId)) {
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
            execute(transfer,userId,role.name());
        } else {
            audit.record("TRANSFER_PENDING_APPROVAL", userId, role.name(), transfer.getId(),
                Map.of("amount", amount.toString(),"previousStatus","NONE",
                    "newStatus",transfer.getStatus().name(),"createdAt",transfer.getCreatedAt().toString(),
                    "sourceAccount",sourceId,"destinationAccount",destinationId));
        }
        return transfer;
    }

    @Transactional
    public void approve(String id, String supervisorId, Role role, String companyId) {
        authorization.require(role, AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
        BankTransfer transfer = require(id);
        requireCompanyScope(transfer, role, companyId);
        TransferStatus previousStatus=transfer.getStatus();
        transfer.expire();
        if (transfer.getStatus() == TransferStatus.EXPIRED) {
            transfers.save(transfer);
            audit.record("TRANSFER_EXPIRED", supervisorId, role.name(), id,
                Map.of("previousStatus",previousStatus.name(),"newStatus",transfer.getStatus().name(),
                    "expiredAt",java.time.Instant.now().toString(),
                    "reason", "vencida por falta de aprobación en el tiempo establecido",
                    "createdAt", transfer.getCreatedAt().toString(),
                    "creatorUserId", transfer.getCreatorUserId()));
            return;
        }
        BankAccount source = accounts.findByAccountNumber(transfer.getSourceAccount())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }
        if (source.getBalance().getAmountDecimal().compareTo(transfer.getAmount().getAmountDecimal()) < 0) {
            throw new IllegalStateException("Insufficient funds for company transfer approval");
        }
        TransferStatus previousStatus=transfer.getStatus();
        transfer.approve(supervisorId);
        transfers.save(transfer);
        execute(transfer,supervisorId,role.name());
        audit.record("TRANSFER_APPROVED", supervisorId, role.name(), transfer.getId(),
            Map.of("previousStatus",previousStatus.name(),"newStatus",transfer.getStatus().name(),
                "createdBy",transfer.getCreatorUserId(),"approvedBy",transfer.getApproverUserId(),
                "approvedAt",transfer.getApprovedAt().toString()));
    }

    @Transactional
    public void reject(String id, String supervisorId, Role role, String companyId) {
        authorization.require(role, AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
        BankTransfer transfer = require(id);
        requireCompanyScope(transfer, role, companyId);
        TransferStatus previousStatus=transfer.getStatus();
        transfer.expire();
        if (transfer.getStatus() == TransferStatus.EXPIRED) {
            transfers.save(transfer);
            audit.record("TRANSFER_EXPIRED", supervisorId, role.name(), id,
                Map.of("previousStatus",previousStatus.name(),"newStatus",transfer.getStatus().name(),
                    "expiredAt",java.time.Instant.now().toString(),"creatorUserId",transfer.getCreatorUserId()));
            return;
        }
        transfer.reject(supervisorId);
        transfers.save(transfer);
        audit.record("TRANSFER_REJECTED", supervisorId, role.name(), id,
            Map.of("previousStatus",previousStatus.name(),"newStatus",transfer.getStatus().name(),
                "createdBy",transfer.getCreatorUserId(),"rejectedBy",transfer.getApproverUserId(),
                "rejectedAt",transfer.getApprovedAt().toString()));
    }

    @Transactional
    @Scheduled(fixedDelayString = "${bank.transfers.expiration-delay-ms:60000}")
    public void expirePending() {
        transfers.findAll().forEach(transfer -> {
            TransferStatus before = transfer.getStatus();
            transfer.expire();
            if (before != transfer.getStatus()) {
                transfers.save(transfer);
                audit.record("TRANSFER_EXPIRED", "SYSTEM", "SYSTEM", transfer.getId(),
                    Map.of("previousStatus",before.name(),"newStatus",transfer.getStatus().name(),
                        "expiredAt",java.time.Instant.now().toString(),
                        "reason", "vencida por falta de aprobación en el tiempo establecido",
                        "createdAt", transfer.getCreatedAt().toString(),
                        "creatorUserId", transfer.getCreatorUserId()));
            }
        });
    }

    private void requireCompanyScope(BankTransfer transfer, Role role, String companyId) {
        if (role == Role.ADMINISTRATOR) return;
        BankAccount source = accounts.findByAccountNumber(transfer.getSourceAccount())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        if (companyId == null || !source.getOwnerId().equals(companyId)) {
            throw new SecurityException("Supervisor cannot approve transfers for another company");
        }
    }

    private BankTransfer require(String id) {
        return transfers.findById(id).orElseThrow(() -> new IllegalArgumentException("Transfer not found"));
    }

    private void execute(BankTransfer transfer,String actorId,String actorRole) {
        TransferStatus previousStatus=transfer.getStatus();
        BankAccount source = accounts.findByAccountNumber(transfer.getSourceAccount())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        BankAccount target = accounts.findByAccountNumber(transfer.getDestinationAccount())
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        if (source.getStatus() != AccountStatus.ACTIVE || target.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Both accounts must be active at execution time");
        }
        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new IllegalArgumentException("Account currencies must match");
        }
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
        Map<String,Object> details=new java.util.LinkedHashMap<>();
        details.put("previousStatus",previousStatus.name());details.put("newStatus",transfer.getStatus().name());
        details.put("amount",transfer.getAmount().toString());details.put("creatorUserId",transfer.getCreatorUserId());
        details.put("source",transfer.getSourceAccount());details.put("sourceBalanceBefore",sourceBefore.toString());
        details.put("sourceBalanceAfter",source.getBalance().toString());
        details.put("destination",transfer.getDestinationAccount());details.put("destinationBalanceBefore",targetBefore.toString());
        details.put("destinationBalanceAfter",target.getBalance().toString());
        if(transfer.getApproverUserId()!=null){details.put("approverUserId",transfer.getApproverUserId());details.put("approvedAt",transfer.getApprovedAt().toString());}
        audit.record("TRANSFER_EXECUTED",actorId,actorRole,transfer.getId(),details);
    }
}
