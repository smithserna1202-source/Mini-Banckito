package application.adapters.jpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataBankProductRepository extends JpaRepository<BankProductEntity,String> {
 List<BankProductEntity> findByActiveTrue();
}
