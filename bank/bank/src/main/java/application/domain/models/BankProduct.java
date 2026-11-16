package application.domain.models;

import application.domain.enums.ProductType;
import java.math.BigDecimal;
import java.util.Objects;

public final class BankProduct {
    private final String id;
    private final String name;
    private final String description;
    private final ProductType type;
    private final BigDecimal interestRate;
    private final boolean requiresApproval;
    private boolean active = true;

    public BankProduct(String id, String name, ProductType type, String description, BigDecimal interestRate) {
        this(id, name, type, description, interestRate, false);
    }

    public BankProduct(String id, String name, ProductType type, String description,
                       BigDecimal interestRate, boolean requiresApproval) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product id and name are required");
        }
        this.id = id;
        this.name = name;
        this.type = Objects.requireNonNull(type);
        this.description = description == null ? "" : description;
        if (interestRate != null && interestRate.signum() < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        this.interestRate = interestRate;
        this.requiresApproval = requiresApproval;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public ProductType getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getInterestRate() { return interestRate; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
}
