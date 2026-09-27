package application.infrastructure;

import application.adapters.InMemoryBankAccountRepository;
import application.adapters.InMemoryCustomerRepository;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.services.CreateAccountService;
import application.domain.services.RegisterCustomerService;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;

public class ConsoleApplication {
    public static void main(String[] args) {
        System.out.println("=== Mini-Banckito Hexagonal Architecture Application ===");

        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryBankAccountRepository accountRepo = new InMemoryBankAccountRepository();

        RegisterCustomerService registerService = new RegisterCustomerService();
        Customer customer = registerService.register("CUS-001", "Manuela Serna", "manuela@example.com");
        customerRepo.save(customer);

        CreateAccountService createAccountService = new CreateAccountService(accountRepo);
        BankAccount account = new BankAccount(new AccountNumber("1234567890"), new Money(500.0));
        createAccountService.createAccount(account);

        System.out.println("Customer registered: " + customer.getFullName());
        System.out.println("Account created with balance: $" + account.getBalance().getAmount());
    }
}
