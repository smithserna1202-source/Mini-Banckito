package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;

public class PayUtilityBillService {
    public void payBill(BankAccount account, double billAmount, String utilityName) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (utilityName == null || utilityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Utility provider name cannot be empty");
        }
        Money amount = new Money(billAmount);
        account.withdraw(amount);
    }
}
