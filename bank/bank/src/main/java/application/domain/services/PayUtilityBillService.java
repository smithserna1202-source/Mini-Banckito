package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class PayUtilityBillService {
    public void payBill(BankAccount account, Money amount) { account.withdraw(amount); }
}
