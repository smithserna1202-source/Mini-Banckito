package application.adapters.jpa;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataBankAccountRepository extends JpaRepository<BankAccountEntity,String> {}