package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
public class GetAccountBalanceService {
    public Money checkBalance(BankAccount account) { return account.getBalance(); }
}
