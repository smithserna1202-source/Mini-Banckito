package application.domain.models;

import application.domain.enums.TransactionType;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Objects;

public class Transaction {
    private final String id;
    private final String accountId;
    private final Money amount;
    private final TransactionType type;
    private final Instant createdAt;

    public Transaction(String id, String accountId, Money amount, TransactionType type) {
        this(id, accountId, amount, type, Instant.now());
    }

    public Transaction(String id, String accountId, Money amount, TransactionType type, Instant createdAt) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Transaction id is required");
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("Account id is required");
        this.id = id;
        this.accountId = accountId;
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        if (!amount.isPositive()) throw new IllegalArgumentException("Transaction amount must be positive");
        this.type = Objects.requireNonNull(type, "Transaction type is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation date is required");
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public Money getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}
