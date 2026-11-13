package application.domain.services;

import application.domain.enums.AccountStatus;
import application.domain.enums.LoanStatus;
import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.LoanRepository;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class LoanService {
 private final LoanRepository loans;
 private final BankAccountRepository accounts;
 private final AuthorizationService authorization;
 private final AuditService audit;
 public LoanService(LoanRepository loans,BankAccountRepository accounts,AuthorizationService authorization,AuditService audit){
  this.loans=Objects.requireNonNull(loans);this.accounts=Objects.requireNonNull(accounts);
  this.authorization=Objects.requireNonNull(authorization);this.audit=Objects.requireNonNull(audit);
 }
 public void request(Loan loan){
  Objects.requireNonNull(loan);
  if(loans.findById(loan.getId()).isPresent())throw new IllegalArgumentException("Loan id already exists");
  BankAccount destination=accounts.findByAccountNumber(loan.getDestinationAccount())
    .orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!destination.getOwnerId().equals(loan.getApplicantId()))throw new SecurityException("Destination account must belong to the applicant");
  if(destination.getStatus()!=AccountStatus.ACTIVE)throw new IllegalStateException("Destination account must be active");
  loans.save(loan);
  audit.record("LOAN_REQUESTED",loan.getApplicantId(),"CUSTOMER",loan.getId(),Map.of("amount",loan.getRequestedAmount().toString()));
 }
 public void approve(String loanId,String analystId,Role role,Money amount,BigDecimal rate){
  authorization.require(role,AuthorizationService.Action.APPROVE_LOAN);
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  loan.approve(analystId,amount,rate);loans.save(loan);
  audit.record("LOAN_APPROVED",analystId,role.name(),loanId,Map.of("amount",amount.toString(),"rate",rate.toPlainString()));
 }
 public void reject(String loanId,String analystId,Role role){
  authorization.require(role,AuthorizationService.Action.APPROVE_LOAN);
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  loan.reject(analystId);loans.save(loan);audit.record("LOAN_REJECTED",analystId,role.name(),loanId,Map.of());
 }
 public void disburse(String loanId){
  Loan loan=loans.findById(loanId).orElseThrow(()->new IllegalArgumentException("Loan not found"));
  if(loan.getStatus()!=LoanStatus.APPROVED||loan.getApprovedAmount()==null||!loan.getApprovedAmount().isPositive())
   throw new IllegalStateException("Only an approved loan with a positive approved amount can be disbursed");
  BankAccount account=accounts.findByAccountNumber(loan.getDestinationAccount()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!account.getOwnerId().equals(loan.getApplicantId()))throw new SecurityException("Destination account does not belong to the applicant");
  if(account.getStatus()!=AccountStatus.ACTIVE)throw new IllegalStateException("Destination account is not active");
  account.deposit(loan.getApprovedAmount());
  loan.markDisbursed();
  accounts.save(account);loans.save(loan);
  audit.record("LOAN_DISBURSED","SYSTEM","SYSTEM",loanId,Map.of("amount",loan.getApprovedAmount().toString(),"account",account.getAccountNumber().getValue()));
 }
}
