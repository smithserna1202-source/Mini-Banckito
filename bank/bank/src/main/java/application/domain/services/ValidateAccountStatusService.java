package application.domain.services;
import application.domain.enums.AccountStatus;
import application.domain.models.BankAccount;
public class ValidateAccountStatusService {
 public boolean isOperational(BankAccount account){return account!=null&&account.getStatus()==AccountStatus.ACTIVE;}
 public void requireOperational(BankAccount account){if(!isOperational(account))throw new IllegalStateException("Account is not operational");}
}