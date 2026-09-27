package application.domain.services;

import application.domain.models.Customer;

public class UpdateCustomerPhoneService {
    public void updatePhone(Customer customer, String phone) {
        if (customer == null || phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }
}
