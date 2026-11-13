package application.adapters;
import application.domain.models.Loan;
import application.domain.ports.LoanRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class InMemoryLoanRepository implements LoanRepository {
 private final ConcurrentMap<String,Loan> loans=new ConcurrentHashMap<>();
 @Override public void save(Loan loan){loans.put(loan.getId(),loan);}
 @Override public Optional<Loan> findById(String id){return Optional.ofNullable(loans.get(id));}
}