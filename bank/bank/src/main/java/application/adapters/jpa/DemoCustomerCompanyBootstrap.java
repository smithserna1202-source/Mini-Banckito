package application.adapters.jpa;

import application.domain.models.Company;
import application.domain.models.Customer;
import application.domain.ports.CompanyRepository;
import application.domain.ports.CustomerRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Creates the profile records linked to the documented demo customer and company users. */
@Component
@Profile("sql")
public class DemoCustomerCompanyBootstrap implements ApplicationRunner {
    private final CustomerRepository customers;
    private final CompanyRepository companies;

    public DemoCustomerCompanyBootstrap(CustomerRepository customers, CompanyRepository companies) {
        this.customers = customers;
        this.companies = companies;
    }

    @Override
    public void run(ApplicationArguments args) {
        customers.findById("C001").orElseGet(() -> {
            Customer customer = new Customer("C001", "natural-customer", "DEMO-C001",
                new Email("natural-customer@demo.local"), new PhoneNumber("3000000000"),
                "Datos de demostración", LocalDate.of(1990, 1, 1), true);
            customers.save(customer);
            return customer;
        });

        if (companies.findById("COMPANY001").isEmpty()) {
            companies.save(new Company("COMPANY001", "Demo Company", "900000001-1",
                new Email("company001@example.test"), new PhoneNumber("3000000001"),
                "Datos de demostración", "C001"));
        }
    }
}
