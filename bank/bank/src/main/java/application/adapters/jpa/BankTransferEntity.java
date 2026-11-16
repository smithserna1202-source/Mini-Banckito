package application.adapters.jpa;

import application.domain.enums.TransferStatus;
import application.domain.models.BankTransfer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "company_bank_transfers", indexes = {
    @Index(name = "idx_company_transfer_status_created", columnList = "status,createdAt")
})
public class BankTransferEntity {
    @Id private String id;
    @Column(nullable = false) private String sourceAccount;
    @Column(nullable = false) private String destinationAccount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private String creatorUserId;
    @Column(nullable = false) private boolean requiresApproval;
    @Column(nullable = false) private Instant createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TransferStatus status;
    private String approverUserId;
    private Instant approvedAt;

    protected BankTransferEntity() {}

    public BankTransferEntity(BankTransfer transfer) {
        id = transfer.getId();
        sourceAccount = transfer.getSourceAccount();
        destinationAccount = transfer.getDestinationAccount();
        amount = transfer.getAmount().getAmountDecimal();
        creatorUserId = transfer.getCreatorUserId();
        requiresApproval = transfer.isRequiresApproval();
        createdAt = transfer.getCreatedAt();
        status = transfer.getStatus();
        approverUserId = transfer.getApproverUserId();
        approvedAt = transfer.getApprovedAt();
    }

    public BankTransfer toDomain() {
        return BankTransfer.restore(id, sourceAccount, destinationAccount,
            new application.domain.valueobjects.Money(amount), creatorUserId, requiresApproval,
            createdAt, status, approverUserId, approvedAt);
    }
}
