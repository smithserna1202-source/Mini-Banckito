package application.adapters.jpa;
import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class CustomerJpaAdapter implements CustomerRepository {
 private final SpringDataCustomerRepository repository;
 public CustomerJpaAdapter(SpringDataCustomerRepository repository){this.repository=repository;}
 @Override public void save(Customer customer){repository.save(new CustomerEntity(customer));}
 @Override public Optional<Customer> findById(String id){
  return repository.findById(id).map(e->new Customer(e.getId(),e.getName(),e.getIdentification(),new Email(e.getEmail()),e.getPhone()==null?null:new PhoneNumber(e.getPhone()),e.getAddress(),e.getBirthDate(),e.isActive()));
 }
}