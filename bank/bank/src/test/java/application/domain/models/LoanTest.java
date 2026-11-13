package application.domain.models;
import application.domain.enums.LoanStatus;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LoanTest {
 @Test void loanMustBeApprovedBeforeDisbursement(){
  Loan loan=new Loan("L1","C1","PERSONAL",new Money(1000),12,"1234567890");
  assertEquals(LoanStatus.UNDER_REVIEW,loan.getStatus());
  assertThrows(IllegalStateException.class,loan::markDisbursed);
  loan.approve("A1",new Money(900),new BigDecimal("0.02"));
  loan.markDisbursed();assertEquals(LoanStatus.DISBURSED,loan.getStatus());
 }
 @Test void approvalCannotExceedRequestedAmount(){
  Loan loan=new Loan("L1","C1","PERSONAL",new Money(1000),12,"1234567890");
  assertThrows(IllegalArgumentException.class,()->loan.approve("A1",new Money(1200),BigDecimal.ZERO));
 }
}