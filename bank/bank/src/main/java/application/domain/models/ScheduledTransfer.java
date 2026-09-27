package application.domain.models;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;

public class ScheduledTransfer {
    private final String id;
    private final AccountNumber source;
    private final AccountNumber destination;
    private final Money amount;

    public ScheduledTransfer(String id, AccountNumber source, AccountNumber destination, Money amount) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    public String getId() { return id; }
    public AccountNumber getSource() { return source; }
    public AccountNumber getDestination() { return destination; }
    public Money getAmount() { return amount; }
}
