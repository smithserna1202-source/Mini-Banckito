package application.domain.models;
import application.domain.enums.AccountStatus;
import application.domain.enums.AccountType;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Objects;
public class BankAccount {
 private final AccountNumber accountNumber; private Money balance;
 private final String ownerId,currency; private final AccountType type; private AccountStatus status;
 private final Instant openedAt;
 public BankAccount(AccountNumber number,Money initialBalance){this(number,initialBalance,"UNASSIGNED",AccountType.SAVINGS,"COP");}
 public BankAccount(AccountNumber number,Money initialBalance,String ownerId,AccountType type,String currency){
  this.accountNumber=Objects.requireNonNull(number);this.balance=Objects.requireNonNull(initialBalance);
  if(ownerId==null||ownerId.isBlank())throw new IllegalArgumentException("Account owner is required");
  if(currency==null||!currency.matches("[A-Z]{3}"))throw new IllegalArgumentException("Currency must be a 3-letter ISO code");
  this.ownerId=ownerId;this.type=Objects.requireNonNull(type);this.currency=currency;this.status=AccountStatus.ACTIVE;this.openedAt=Instant.now();
 }
 public AccountNumber getAccountNumber(){return accountNumber;} public Money getBalance(){return balance;}
 public String getOwnerId(){return ownerId;} public AccountType getType(){return type;} public String getCurrency(){return currency;}
 public AccountStatus getStatus(){return status;} public Instant getOpenedAt(){return openedAt;}
 public void deposit(Money amount){ensureOperational();requirePositive(amount);balance=balance.add(amount);}
 public void withdraw(Money amount){ensureOperational();requirePositive(amount);if(amount.isGreaterThan(balance))throw new IllegalArgumentException("Insufficient funds");balance=balance.subtract(amount);}
 public void freeze(){if(status!=AccountStatus.ACTIVE)throw new IllegalStateException("Only active accounts can be frozen");status=AccountStatus.FROZEN;}
 public void unfreeze(){if(status!=AccountStatus.FROZEN)throw new IllegalStateException("Only frozen accounts can be reactivated");status=AccountStatus.ACTIVE;}
 public void close(){if(balance.isPositive())throw new IllegalStateException("An account with a balance cannot be closed");status=AccountStatus.CLOSED;}
 private void ensureOperational(){if(status!=AccountStatus.ACTIVE)throw new IllegalStateException("Account is not operational: "+status);}
 private void requirePositive(Money amount){if(amount==null||!amount.isPositive())throw new IllegalArgumentException("Amount must be positive");}
}