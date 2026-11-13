package application.domain.services;
import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import java.util.Objects;
public class GetCustomerProfileService {
 private final CustomerRepository repository;
 public GetCustomerProfileService(CustomerRepository repository){this.repository=Objects.requireNonNull(repository);}
 public Customer getProfile(String customerId){return repository.findById(customerId).orElseThrow(()->new IllegalArgumentException("Customer not found"));}
}