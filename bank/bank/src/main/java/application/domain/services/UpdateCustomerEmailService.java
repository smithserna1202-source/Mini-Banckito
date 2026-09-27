package application.domain.services;

import application.domain.models.Customer;
import application.domain.valueobjects.Email;

public class UpdateCustomerEmailService {
    public void updateEmail(Customer customer, String newEmail) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customer.setEmail(new Email(newEmail));
    }
}
