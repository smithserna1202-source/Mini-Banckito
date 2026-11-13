package application.domain.models;
import application.domain.enums.LoanStatus;
import application.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
public final class Loan {
 private final String id,applicantId,loanType,destinationAccount; private final Money requestedAmount;
 private final int termMonths; private Money approvedAmount; private BigDecimal interestRate;
 private LoanStatus status=LoanStatus.UNDER_REVIEW; private final Instant requestedAt=Instant.now(); private Instant approvedAt,disbursedAt;
 public Loan(String id,String applicantId,String loanType,Money requestedAmount,int termMonths,String destinationAccount){
  if(id==null||id.isBlank()||applicantId==null||applicantId.isBlank()||loanType==null||loanType.isBlank())throw new IllegalArgumentException("Loan identity fields are required");
  if(requestedAmount==null||!requestedAmount.isPositive()||termMonths<=0)throw new IllegalArgumentException("Loan amount and term must be positive");
  this.id=id;this.applicantId=applicantId;this.loanType=loanType;this.requestedAmount=requestedAmount;this.termMonths=termMonths;this.destinationAccount=Objects.requireNonNull(destinationAccount);
 }
 public void approve(String analystId,Money amount,BigDecimal rate){
  requireReview();if(analystId==null||analystId.isBlank())throw new IllegalArgumentException("Analyst is required");
  if(amount==null||!amount.isPositive()||amount.isGreaterThan(requestedAmount))throw new IllegalArgumentException("Approved amount must be positive and not exceed requested amount");
  if(rate==null||rate.signum()<0)throw new IllegalArgumentException("Interest rate cannot be negative");
  approvedAmount=amount;interestRate=rate;approvedAt=Instant.now();status=LoanStatus.APPROVED;
 }
 public void reject(String analystId){requireReview();if(analystId==null||analystId.isBlank())throw new IllegalArgumentException("Analyst is required");status=LoanStatus.REJECTED;}
 public void markDisbursed(){if(status!=LoanStatus.APPROVED||approvedAmount==null||!approvedAmount.isPositive())throw new IllegalStateException("Only approved loans with a valid amount can be disbursed");status=LoanStatus.DISBURSED;disbursedAt=Instant.now();}
 private void requireReview(){if(status!=LoanStatus.UNDER_REVIEW)throw new IllegalStateException("Loan is not under review");}
 public String getId(){return id;} public String getApplicantId(){return applicantId;} public String getLoanType(){return loanType;}
 public Money getRequestedAmount(){return requestedAmount;} public Money getApprovedAmount(){return approvedAmount;} public BigDecimal getInterestRate(){return interestRate;}
 public int getTermMonths(){return termMonths;} public LoanStatus getStatus(){return status;} public Instant getRequestedAt(){return requestedAt;}
 public Instant getApprovedAt(){return approvedAt;} public Instant getDisbursedAt(){return disbursedAt;} public String getDestinationAccount(){return destinationAccount;}
}