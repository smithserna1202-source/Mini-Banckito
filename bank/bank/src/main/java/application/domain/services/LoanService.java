package application.domain.services;

import application.domain.enums.AccountStatus;
import application.domain.enums.LoanStatus;
import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.LoanRepository;
import application.domain.ports.CustomerRepository;
import application.domain.ports.CompanyRepository;
import application.domain.ports.BankProductRepository;
import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

public class LoanService {
 private final LoanRepository loans;
 private final BankAccountRepository accounts;
 private final AuthorizationService authorization;
 private final AuditService audit;
 private final CustomerRepository customers;
 private final CompanyRepository companies;
 private final BankProductRepository products;
 public LoanService(LoanRepository loans,BankAccountRepository accounts,AuthorizationService authorization,AuditService audit){
  this(loans,accounts,authorization,audit,null,null,null);
 }
 public LoanService(LoanRepository loans,BankAccountRepository accounts,AuthorizationService authorization,
                    AuditService audit,CustomerRepository customers,CompanyRepository companies){
  this(loans,accounts,authorization,audit,customers,companies,null);
 }
 public LoanService(LoanRepository loans,BankAccountRepository accounts,AuthorizationService authorization,
                    AuditService audit,CustomerRepository customers,CompanyRepository companies,
                    BankProductRepository products){
  this.loans=Objects.requireNonNull(loans);this.accounts=Objects.requireNonNull(accounts);
  this.authorization=Objects.requireNonNull(authorization);this.audit=Objects.requireNonNull(audit);
  this.customers=customers;this.companies=companies;this.products=products;
 }
 @Transactional
 public void request(Loan loan){
  request(loan,loan.getApplicantId(),Role.NATURAL_PERSON_CUSTOMER);
 }
 @Transactional
 public void request(Loan loan,String requesterId,Role requesterRole){
  Objects.requireNonNull(loan);
  Objects.requireNonNull(requesterId);
  Objects.requireNonNull(requesterRole);
  if(products!=null&&products.findAllActive().stream().noneMatch(p->p.getType()==ProductType.LOAN&&p.isRequiresApproval()))
   throw new IllegalStateException("Loan product is not available in the active product catalog");
  if(customers!=null&&companies!=null){
   boolean activeCustomer=customers.findById(loan.getApplicantId())
    .filter(application.domain.models.Customer::isActive).isPresent();
   boolean activeCompany=companies.findById(loan.getApplicantId())
    .filter(application.domain.models.Company::isActive).isPresent();
   if(!activeCustomer&&!activeCompany)throw new IllegalStateException("Loan applicant must be an existing active customer or company");
  }
  if(loans.findById(loan.getId()).isPresent())throw new IllegalArgumentException("Loan id already exists");
  BankAccount destination=accounts.findByAccountNumber(loan.getDestinationAccount())
    .orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!destination.getOwnerId().equals(loan.getApplicantId()))throw new SecurityException("Destination account must belong to the applicant");
  if(destination.getStatus()!=AccountStatus.ACTIVE)throw new IllegalStateException("Destination account must be active");
  loans.save(loan);
  audit.record("LOAN_REQUESTED",requesterId,requesterRole.name(),loan.getId(),
   Map.of("applicantId",loan.getApplicantId(),"amount",loan.getRequestedAmount().toString(),
    "previousStatus","NONE","newStatus",loan.getStatus().name(),"requestedAt",loan.getRequestedAt().toString()));
 }
 @Transactional
 public void approve(String loanId,String analystId,Role role,Money amount,BigDecimal rate){
  if(role!=Role.INTERNAL_ANALYST)throw new SecurityException("Only an internal analyst may approve loans");
  authorization.require(role,AuthorizationService.Action.APPROVE_LOAN);
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  LoanStatus previousStatus=loan.getStatus();
  loan.approve(analystId,amount,rate);loans.save(loan);
  audit.record("LOAN_APPROVED",analystId,role.name(),loanId,
   Map.of("previousStatus",previousStatus.name(),"newStatus",loan.getStatus().name(),
    "amount",amount.toString(),"rate",rate.toPlainString(),
    "approvedAt",loan.getApprovedAt().toString(),"analystId",analystId));
 }
 @Transactional
 public void reject(String loanId,String analystId,Role role){
  if(role!=Role.INTERNAL_ANALYST)throw new SecurityException("Only an internal analyst may reject loans");
  authorization.require(role,AuthorizationService.Action.APPROVE_LOAN);
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  LoanStatus previousStatus=loan.getStatus();
  loan.reject(analystId);loans.save(loan);
  audit.record("LOAN_REJECTED",analystId,role.name(),loanId,
   Map.of("previousStatus",previousStatus.name(),"newStatus",loan.getStatus().name(),"analystId",analystId));
 }
 @Transactional
 public void disburse(String loanId,String analystId,Role role){
  if(role!=Role.INTERNAL_ANALYST)throw new SecurityException("Only an internal analyst may disburse loans");
  authorization.require(role,AuthorizationService.Action.APPROVE_LOAN);
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  if(loan.getStatus()!=LoanStatus.APPROVED||loan.getApprovedAmount()==null||!loan.getApprovedAmount().isPositive())
   throw new IllegalStateException("Only an approved loan with a positive approved amount can be disbursed");
  LoanStatus previousStatus=loan.getStatus();
  BankAccount account=accounts.findByAccountNumber(loan.getDestinationAccount()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!account.getOwnerId().equals(loan.getApplicantId()))throw new SecurityException("Destination account does not belong to the applicant");
  if(account.getStatus()!=AccountStatus.ACTIVE)throw new IllegalStateException("Destination account is not active");
  account.deposit(loan.getApprovedAmount());
  loan.markDisbursed();
  accounts.save(account);loans.save(loan);
  audit.record("LOAN_DISBURSED",analystId,role.name(),loanId,
   Map.of("previousStatus",previousStatus.name(),"newStatus",loan.getStatus().name(),
    "amount",loan.getApprovedAmount().toString(),"account",account.getAccountNumber().getValue(),
    "disbursedAt",loan.getDisbursedAt().toString(),"analystId",analystId));
 }
}
