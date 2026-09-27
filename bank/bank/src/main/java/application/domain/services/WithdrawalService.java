package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class WithdrawalService {
    public void withdraw(BankAccount account, Money amount) { account.withdraw(amount); }
}
