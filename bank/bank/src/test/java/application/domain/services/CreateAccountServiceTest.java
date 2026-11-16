package application.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryCompanyRepository;
import application.adapters.InMemoryCustomerRepository;
import application.domain.enums.AccountType;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Company;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CreateAccountServiceTest {
    @Test
    void accountCanOnlyBeOpenedForExistingActiveCustomer() {
        InMemoryBankAccountRepository accounts = new InMemoryBankAccountRepository();
        InMemoryCustomerRepository customers = new InMemoryCustomerRepository();
        InMemoryCompanyRepository companies = new InMemoryCompanyRepository();
        Customer active = new Customer("C001", "Active Customer", "ID001",
            new Email("active@example.com"), new PhoneNumber("3001234567"),
            "Medellin", LocalDate.of(1990, 1, 1), true);
        Customer inactive = new Customer("C002", "Inactive Customer", "ID002",
            new Email("inactive@example.com"), new PhoneNumber("3001234568"),
            "Medellin", LocalDate.of(1990, 1, 1), false);
        customers.save(active);
        customers.save(inactive);
        Customer blocked = new Customer("C003", "Blocked Customer", "ID003",
            new Email("blocked@example.com"), new PhoneNumber("3001234569"),
            "Medellin", LocalDate.of(1990, 1, 1), true);
        blocked.block();
        customers.save(blocked);
        Company blockedCompany = new Company("B001", "Blocked Company", "900000001",
            new Email("blocked-company@example.com"), new PhoneNumber("3001234570"),
            "Medellin", "C001");
        blockedCompany.block();
        companies.save(blockedCompany);
        CreateAccountService service = new CreateAccountService(accounts, customers, companies);

        service.createAccount(new BankAccount(new AccountNumber("7000000001"),
            Money.zero(), "C001", AccountType.SAVINGS, "COP"));

        assertThrows(IllegalArgumentException.class, () -> service.createAccount(
            new BankAccount(new AccountNumber("7000000002"), Money.zero(), "C002",
                AccountType.SAVINGS, "COP")));
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(
            new BankAccount(new AccountNumber("7000000003"), Money.zero(), "UNKNOWN",
                AccountType.SAVINGS, "COP")));
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(
            new BankAccount(new AccountNumber("7000000004"), Money.zero(), "C003",
                AccountType.SAVINGS, "COP")));
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(
            new BankAccount(new AccountNumber("7000000005"), Money.zero(), "B001",
                AccountType.BUSINESS, "COP")));
    }
}
