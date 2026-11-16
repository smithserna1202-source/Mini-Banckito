package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Money;
import java.util.Objects;
public class InternalTransferService {
 public void transfer(BankAccount source,BankAccount target,Money amount){
  Objects.requireNonNull(source);Objects.requireNonNull(target);
  if(source==target||source.getAccountNumber().getValue().equals(target.getAccountNumber().getValue()))throw new IllegalArgumentException("Source and destination accounts must differ");
  if(!source.getCurrency().equals(target.getCurrency()))throw new IllegalArgumentException("Account currencies must match");
  if(amount==null||!amount.isPositive())throw new IllegalArgumentException("Transfer amount must be positive");
  source.withdraw(amount);
  try{target.deposit(amount);}catch(RuntimeException ex){source.deposit(amount);throw ex;}
 }
}