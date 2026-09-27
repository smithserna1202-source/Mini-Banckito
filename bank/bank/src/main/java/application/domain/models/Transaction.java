package application.domain.models;

import application.domain.enums.TransactionType;
import application.domain.valueobjects.Money;

public class Transaction {
    private final String id;
    private final String accountId;
    private final Money amount;
    private final TransactionType type;

    public Transaction(String id, String accountId, Money amount, TransactionType type) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public Money getAmount() { return amount; }
    public TransactionType getType() { return type; }
}
