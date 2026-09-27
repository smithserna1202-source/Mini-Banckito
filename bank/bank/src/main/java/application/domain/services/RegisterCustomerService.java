package application.domain.services;
import application.domain.models.Customer;
import application.domain.valueobjects.Email;
public class RegisterCustomerService {
    public Customer register(String id, String name, String email) { return new Customer(id, name, new Email(email)); }
}
