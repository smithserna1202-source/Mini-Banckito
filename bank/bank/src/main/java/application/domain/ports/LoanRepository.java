package application.domain.ports;
import application.domain.models.Loan;
import java.util.Optional;
public interface LoanRepository { void save(Loan loan); Optional<Loan> findById(String id); }