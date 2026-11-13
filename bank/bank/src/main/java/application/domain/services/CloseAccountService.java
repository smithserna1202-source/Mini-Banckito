package application.domain.services;
import application.domain.models.BankAccount;
public class CloseAccountService { public void close(BankAccount account){account.close();} }