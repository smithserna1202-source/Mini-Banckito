package application.adapters.jpa;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity,String> {}