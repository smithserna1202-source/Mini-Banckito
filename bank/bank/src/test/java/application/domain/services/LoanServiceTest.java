package application.domain.services;

import application.adapters.InMemoryAuditLogRepository;
import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryLoanRepository;
import application.domain.enums.AccountType;
import application.domain.enums.LoanStatus;
import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {
 @Test void unapprovedLoanCannotChangeDestinationBalance(){
  InMemoryLoanRepository loans=new InMemoryLoanRepository();
  InMemoryBankAccountRepository accounts=new InMemoryBankAccountRepository();
  AuditService audit=new AuditService(new InMemoryAuditLogRepository());
  LoanService service=new LoanService(loans,accounts,new AuthorizationService(),audit);
  BankAccount destination=new BankAccount(new AccountNumber("1234567890"),Money.of("50"),"C001",AccountType.SAVINGS,"COP");
  accounts.save(destination);
  Loan loan=new Loan("L001","C001","PERSONAL",Money.of("500"),12,"1234567890");
  service.request(loan);
  assertEquals(LoanStatus.UNDER_REVIEW,loan.getStatus());
  assertThrows(IllegalStateException.class,()->service.disburse("L001"));
  assertEquals(Money.of("50"),destination.getBalance());
 }
 @Test void onlyAnalystMayApproveLoan(){
  InMemoryLoanRepository loans=new InMemoryLoanRepository();
  InMemoryBankAccountRepository accounts=new InMemoryBankAccountRepository();
  LoanService service=new LoanService(loans,accounts,new AuthorizationService(),new AuditService(new InMemoryAuditLogRepository()));
  Loan loan=new Loan("L002","C001","PERSONAL",Money.of("500"),12,"1234567890");
  loans.save(loan);
  assertThrows(SecurityException.class,()->service.approve("L002","teller01",Role.TELLER,Money.of("400"),java.math.BigDecimal.ONE));
 }
}
