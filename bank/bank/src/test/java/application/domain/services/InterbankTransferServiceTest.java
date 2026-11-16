package application.domain.services;

import application.domain.enums.AccountType;
import application.domain.models.BankAccount;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterbankTransferServiceTest {
 @Test void unsupportedExternalTransferNeverDebitsSource(){
  BankAccount source=new BankAccount(new AccountNumber("1234567890"),Money.of("1000"),"C001",AccountType.SAVINGS,"COP");
  InterbankTransferService service=new InterbankTransferService();
  assertThrows(UnsupportedOperationException.class,()->service.transferToExternalBank(source,"001","9876543210",Money.of("100")));
  assertEquals(Money.of("1000"),source.getBalance());
 }
}
