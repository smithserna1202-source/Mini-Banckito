package application.domain.models;
import application.domain.enums.AccountStatus;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AccountRulesTest {
 @Test void frozenAccountCannotMoveMoney(){
  BankAccount account=new BankAccount(new AccountNumber("1234567890"),new Money(100));
  account.freeze();assertEquals(AccountStatus.FROZEN,account.getStatus());
  assertThrows(IllegalStateException.class,()->account.withdraw(new Money(1)));
 }
 @Test void accountRejectsZeroAndNegativeOperations(){
  BankAccount account=new BankAccount(new AccountNumber("1234567890"),new Money(100));
  assertThrows(IllegalArgumentException.class,()->account.deposit(Money.zero()));
  assertThrows(IllegalArgumentException.class,()->account.withdraw(new Money(-1)));
 }
}