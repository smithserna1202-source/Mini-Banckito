package application.domain.services;
import application.domain.models.Customer;
import application.domain.valueobjects.PhoneNumber;
public class UpdateCustomerPhoneService { public void updatePhone(Customer customer,String phone){customer.setPhone(new PhoneNumber(phone));} }