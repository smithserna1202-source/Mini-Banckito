package application.domain.services;

import application.domain.models.Customer;

public class GetCustomerProfileService {
    public Customer getProfile(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return customer;
    }
}
