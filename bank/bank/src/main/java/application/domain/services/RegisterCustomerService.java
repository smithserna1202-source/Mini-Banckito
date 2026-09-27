package application.domain.services;

import application.domain.models.Customer;
import application.domain.valueobjects.Email;

public class RegisterCustomerService {
    public Customer register(String id, String fullName, String emailStr) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        Email email = new Email(emailStr);
        return new Customer(id, fullName, email);
    }
}
