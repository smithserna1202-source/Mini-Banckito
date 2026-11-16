package application.adapters.jpa;

import application.domain.models.ScheduledTransfer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "scheduled_bank_transfers", indexes = {
    @Index(name = "idx_scheduled_transfer_due", columnList = "status,scheduledAt")
})
public class ScheduledTransferEntity {
    @Id private String id;
    @Column(nullable = false) private String sourceAccount;
    @Column(nullable = false) private String destinationAccount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private Instant scheduledAt;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private String creatorId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ScheduledTransfer.Status status;

    protected ScheduledTransferEntity() {}

    public ScheduledTransferEntity(ScheduledTransfer transfer) {
        id = transfer.getId();
        sourceAccount = transfer.getSource().getValue();
        destinationAccount = transfer.getDestination().getValue();
        amount = transfer.getAmount().getAmountDecimal();
        scheduledAt = transfer.getScheduledAt();
        createdAt = transfer.getCreatedAt();
        creatorId = transfer.getCreatorId();
        status = transfer.getStatus();
    }

    public ScheduledTransfer toDomain() {
        return ScheduledTransfer.restore(id,
            new application.domain.valueobjects.AccountNumber(sourceAccount),
            new application.domain.valueobjects.AccountNumber(destinationAccount),
            new application.domain.valueobjects.Money(amount), scheduledAt, creatorId, createdAt, status);
    }
}
