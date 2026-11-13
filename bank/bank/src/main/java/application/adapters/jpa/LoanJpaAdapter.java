package application.adapters.jpa;
import application.domain.enums.LoanStatus;
import application.domain.models.Loan;
import application.domain.ports.LoanRepository;
import application.domain.valueobjects.Money;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class LoanJpaAdapter implements LoanRepository {
 private final SpringDataLoanRepository repository;
 public LoanJpaAdapter(SpringDataLoanRepository repository){this.repository=repository;}
 @Override public void save(Loan loan){repository.save(new LoanEntity(loan));}
 @Override public Optional<Loan> findById(String id){return repository.findById(id).map(this::toDomain);}
 private Loan toDomain(LoanEntity e){
  Loan l=new Loan(e.getId(),e.getApplicantId(),e.getLoanType(),new Money(e.getRequestedAmount()),e.getTermMonths(),e.getDestinationAccount());
  if(e.getStatus()==LoanStatus.REJECTED)l.reject("PERSISTED");
  else if(e.getStatus()==LoanStatus.APPROVED||e.getStatus()==LoanStatus.DISBURSED){
   l.approve("PERSISTED",new Money(e.getApprovedAmount()),e.getInterestRate());
   if(e.getStatus()==LoanStatus.DISBURSED)l.markDisbursed();
  }
  return l;
 }
}
