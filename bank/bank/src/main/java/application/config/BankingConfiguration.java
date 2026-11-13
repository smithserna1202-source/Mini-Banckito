package application.config;

import application.adapters.*;
import application.domain.ports.*;
import application.domain.services.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Supplies in-memory fallbacks for ports not yet backed by a durable adapter.
 * SQL/Mongo adapters take precedence when their Spring profiles are enabled.
 */
@Configuration
@EnableScheduling
public class BankingConfiguration {
 @Bean @ConditionalOnMissingBean(BankAccountRepository.class)
 public BankAccountRepository bankAccountRepository(){return new InMemoryBankAccountRepository();}
 @Bean @ConditionalOnMissingBean(CustomerRepository.class)
 public CustomerRepository customerRepository(){return new InMemoryCustomerRepository();}
 @Bean @ConditionalOnMissingBean(CompanyRepository.class)
 public CompanyRepository companyRepository(){return new InMemoryCompanyRepository();}
 @Bean @ConditionalOnMissingBean(BankProductRepository.class)
 public BankProductRepository bankProductRepository(){return new InMemoryBankProductRepository();}
 @Bean @ConditionalOnMissingBean(TransactionRepository.class)
 public TransactionRepository transactionRepository(){return new InMemoryTransactionRepository();}
 @Bean @ConditionalOnMissingBean
 public GetTransactionHistoryService getTransactionHistoryService(TransactionRepository repository){return new GetTransactionHistoryService(repository);}
 @Bean @ConditionalOnMissingBean(ScheduledTransferRepository.class)
 public ScheduledTransferRepository scheduledTransferRepository(){return new InMemoryScheduledTransferRepository();}
 @Bean @ConditionalOnMissingBean(LoanRepository.class)
 public LoanRepository loanRepository(){return new InMemoryLoanRepository();}
 @Bean @ConditionalOnMissingBean(AuditLogRepository.class)
 public AuditLogRepository auditLogRepository(){return new InMemoryAuditLogRepository();}
 @Bean @ConditionalOnMissingBean
 public AuthorizationService authorizationService(){return new AuthorizationService();}
 @Bean @ConditionalOnMissingBean
 public AuditService auditService(AuditLogRepository repository){return new AuditService(repository);}
 @Bean @ConditionalOnMissingBean
 public CreateAccountService createAccountService(BankAccountRepository repository){return new CreateAccountService(repository);}
 @Bean @ConditionalOnMissingBean
 public DepositService depositService(){return new DepositService();}
 @Bean @ConditionalOnMissingBean
 public WithdrawalService withdrawalService(){return new WithdrawalService();}
 @Bean @ConditionalOnMissingBean
 public GetAccountBalanceService getAccountBalanceService(){return new GetAccountBalanceService();}
 @Bean @ConditionalOnMissingBean
 public LoanService loanService(LoanRepository loans,BankAccountRepository accounts,AuthorizationService authorization,AuditService audit){return new LoanService(loans,accounts,authorization,audit);}
 @Bean @ConditionalOnMissingBean
 public CompanyTransferService companyTransferService(BankAccountRepository accounts,AuthorizationService authorization,AuditService audit){return new CompanyTransferService(accounts,authorization,audit,application.domain.valueobjects.Money.of("5000000"));}
 @Bean @ConditionalOnMissingBean
 public ScheduleTransferService scheduleTransferService(ScheduledTransferRepository transfers,BankAccountRepository accounts,AuditService audit){return new ScheduleTransferService(transfers,accounts,audit);}
 @Bean @ConditionalOnMissingBean
 public CancelScheduledTransferService cancelScheduledTransferService(ScheduledTransferRepository transfers,AuditService audit){return new CancelScheduledTransferService(transfers,audit);}
 @Bean @ConditionalOnMissingBean
 public ExecuteScheduledTransfersService executeScheduledTransfersService(ScheduledTransferRepository transfers,BankAccountRepository accounts,AuditService audit){return new ExecuteScheduledTransfersService(transfers,accounts,audit);}
}