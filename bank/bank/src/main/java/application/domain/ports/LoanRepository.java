package application.domain.ports;
import application.domain.models.Loan;
import java.util.Optional;
import java.util.List;
public interface LoanRepository { void save(Loan loan); Optional<Loan> findById(String id); List<Loan> findAll(); }