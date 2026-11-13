package application.adapters.jpa;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataLoanRepository extends JpaRepository<LoanEntity,String> {}
