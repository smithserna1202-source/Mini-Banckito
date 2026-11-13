package application.adapters.jpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity,String> {
 List<TransactionEntity> findByAccountIdOrderByCreatedAtDesc(String accountId);
}
