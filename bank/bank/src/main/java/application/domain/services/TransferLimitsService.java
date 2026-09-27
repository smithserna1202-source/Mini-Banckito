package application.domain.services;

import application.domain.valueobjects.Money;

public class TransferLimitsService {
    public boolean isWithinLimit(Money amount, Money dailyLimit) {
        if (amount == null || dailyLimit == null) {
            throw new IllegalArgumentException("Amount and limit cannot be null");
        }
        return amount.getAmount() <= dailyLimit.getAmount();
    }
}
