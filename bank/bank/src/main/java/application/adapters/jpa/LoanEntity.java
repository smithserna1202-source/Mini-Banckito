package application.adapters.jpa;
import application.domain.enums.LoanStatus;
import application.domain.models.Loan;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="bank_loans")
public class LoanEntity {
 @Id private String id;
 @Column(nullable=false) private String applicantId;
 @Column(nullable=false) private String loanType;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal requestedAmount;
 @Column(nullable=false) private int termMonths;
 @Column(nullable=false) private String destinationAccount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private LoanStatus status;
 @Column(precision=19,scale=2) private BigDecimal approvedAmount;
 @Column(precision=9,scale=4) private BigDecimal interestRate;
 @Column(nullable=false) private Instant requestedAt;
 @Column private Instant approvedAt;
 @Column private Instant disbursedAt;
 protected LoanEntity(){}
 public LoanEntity(Loan l){id=l.getId();applicantId=l.getApplicantId();loanType=l.getLoanType();requestedAmount=l.getRequestedAmount().getAmountDecimal();termMonths=l.getTermMonths();destinationAccount=l.getDestinationAccount();status=l.getStatus();approvedAmount=l.getApprovedAmount()==null?null:l.getApprovedAmount().getAmountDecimal();interestRate=l.getInterestRate();requestedAt=l.getRequestedAt();approvedAt=l.getApprovedAt();disbursedAt=l.getDisbursedAt();}
 public String getId(){return id;} public String getApplicantId(){return applicantId;} public String getLoanType(){return loanType;}
 public BigDecimal getRequestedAmount(){return requestedAmount;} public int getTermMonths(){return termMonths;} public String getDestinationAccount(){return destinationAccount;}
 public LoanStatus getStatus(){return status;} public BigDecimal getApprovedAmount(){return approvedAmount;} public BigDecimal getInterestRate(){return interestRate;}
}
