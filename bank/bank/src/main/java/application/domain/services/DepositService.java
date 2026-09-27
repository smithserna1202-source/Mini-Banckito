package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class DepositService {
    public void deposit(BankAccount account, Money amount) { account.deposit(amount); }
}
