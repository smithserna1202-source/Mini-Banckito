package application.domain.services;
import application.domain.models.Customer;
import application.domain.valueobjects.Email;
public class UpdateCustomerEmailService {
    public void updateEmail(Customer customer, String email) { customer.setEmail(new Email(email)); }
}
