package application.adapters;

import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final ConcurrentMap<String, Customer> customers = new ConcurrentHashMap<>();
    @Override public void save(Customer customer) { customers.put(customer.getId(), customer); }
    @Override public Optional<Customer> findById(String id) { return Optional.ofNullable(customers.get(id)); }
}
