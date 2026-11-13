package application.adapters.jpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity,String> {
 Optional<CustomerEntity> findByIdentification(String identification);
 Optional<CustomerEntity> findByEmailIgnoreCase(String email);
}
