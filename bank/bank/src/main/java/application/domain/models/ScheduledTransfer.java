package application.domain.models;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Objects;

public final class ScheduledTransfer {
    public enum Status { SCHEDULED, EXECUTED, CANCELLED, FAILED }

    private final String id;
    private final String creatorId;
    private final AccountNumber source;
    private final AccountNumber destination;
    private final Money amount;
    private final Instant scheduledAt;
    private final Instant createdAt;
    private Status status;

    public ScheduledTransfer(String id, AccountNumber source, AccountNumber destination, Money amount) {
        this(id, source, destination, amount, Instant.now().plusSeconds(60), "SYSTEM");
    }

    public ScheduledTransfer(String id, AccountNumber source, AccountNumber destination, Money amount,
                             Instant scheduledAt, String creatorId) {
        this(id, source, destination, amount, scheduledAt, creatorId, Instant.now(), Status.SCHEDULED, true);
    }

    private ScheduledTransfer(String id, AccountNumber source, AccountNumber destination, Money amount,
                              Instant scheduledAt, String creatorId, Instant createdAt, Status status,
                              boolean validateFuture) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Transfer id is required");
        this.source = Objects.requireNonNull(source);
        this.destination = Objects.requireNonNull(destination);
        if (source.getValue().equals(destination.getValue())) throw new IllegalArgumentException("Accounts must differ");
        if (amount == null || !amount.isPositive()) throw new IllegalArgumentException("Amount must be positive");
        this.amount = amount;
        this.id = id;
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.creatorId = Objects.requireNonNull(creatorId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.status = Objects.requireNonNull(status);
        if (validateFuture && !scheduledAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
    }

    public static ScheduledTransfer restore(String id, AccountNumber source, AccountNumber destination,
                                            Money amount, Instant scheduledAt, String creatorId,
                                            Instant createdAt, Status status) {
        return new ScheduledTransfer(id, source, destination, amount, scheduledAt, creatorId,
            createdAt, status, false);
    }

    public void cancel() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Only scheduled transfers can be cancelled");
        status = Status.CANCELLED;
    }

    public void markExecuted() {
        if (status != Status.SCHEDULED) throw new IllegalStateException("Transfer is not scheduled");
        status = Status.EXECUTED;
    }

    public void markFailed() {
        if (status == Status.SCHEDULED) status = Status.FAILED;
    }

    public String getId() { return id; }
    public AccountNumber getSource() { return source; }
    public AccountNumber getDestination() { return destination; }
    public Money getAmount() { return amount; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatorId() { return creatorId; }
    public Status getStatus() { return status; }
}
