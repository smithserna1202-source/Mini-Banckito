package application.config;
import application.adapters.*;
import application.domain.ports.*;
import application.domain.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
@Configuration
@Profile("!sql")
public class BankingConfiguration {
 @Bean public BankAccountRepository bankAccountRepository(){return new InMemoryBankAccountRepository();}
 @Bean public CustomerRepository customerRepository(){return new InMemoryCustomerRepository();}
 @Bean public TransactionRepository transactionRepository(){return new InMemoryTransactionRepository();}
 @Bean public ScheduledTransferRepository scheduledTransferRepository(){return new InMemoryScheduledTransferRepository();}
 @Bean public LoanRepository loanRepository(){return new InMemoryLoanRepository();}
 @Bean @Profile("!mongodb") public AuditLogRepository auditLogRepository(){return new InMemoryAuditLogRepository();}
 @Bean public AuthorizationService authorizationService(){return new AuthorizationService();}
 @Bean public AuditService auditService(AuditLogRepository r){return new AuditService(r);}
 @Bean public CreateAccountService createAccountService(BankAccountRepository r){return new CreateAccountService(r);}
 @Bean public DepositService depositService(){return new DepositService();}
 @Bean public WithdrawalService withdrawalService(){return new WithdrawalService();}
 @Bean public GetAccountBalanceService getAccountBalanceService(){return new GetAccountBalanceService();}
 @Bean public LoanService loanService(LoanRepository l,BankAccountRepository a,AuthorizationService z,AuditService audit){return new LoanService(l,a,z,audit);}
 @Bean public CompanyTransferService companyTransferService(BankAccountRepository a,AuthorizationService z,AuditService audit){return new CompanyTransferService(a,z,audit,application.domain.valueobjects.Money.of("5000000"));}
 @Bean public ScheduleTransferService scheduleTransferService(ScheduledTransferRepository s,BankAccountRepository a,AuditService audit){return new ScheduleTransferService(s,a,audit);}
 @Bean public CancelScheduledTransferService cancelScheduledTransferService(ScheduledTransferRepository s,AuditService a){return new CancelScheduledTransferService(s,a);}
 @Bean public ExecuteScheduledTransfersService executeScheduledTransfersService(ScheduledTransferRepository s,BankAccountRepository a,AuditService audit){return new ExecuteScheduledTransfersService(s,a,audit);}
}