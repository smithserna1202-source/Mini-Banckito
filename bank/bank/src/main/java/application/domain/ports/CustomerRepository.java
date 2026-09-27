package application.domain.ports;

import application.domain.models.Customer;
import java.util.Optional;

public interface CustomerRepository {
    void save(Customer customer);
    Optional<Customer> findById(String id);
}
