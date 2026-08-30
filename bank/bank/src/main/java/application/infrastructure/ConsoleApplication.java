package application.infrastructure;

import application.adapters.InMemoryBankAccountRepository;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.Money;

public class ConsoleApplication {
    public static void main(String[] args) {
        BankAccountRepository repository = new InMemoryBankAccountRepository();

        AccountNumber accNum = new AccountNumber("ACC123");
        Email email = new Email("manuela@example.com");
        Money initialBalance = new Money(500000.0);

        Customer customer = new Customer("1", "Manuela Serna", email);
        BankAccount account = new BankAccount(accNum, initialBalance, customer.getId());

        repository.save(account);

        repository.findByAccountNumber("ACC123").ifPresent(acc -> {
            System.out.println("Bank Account found!");
            System.out.println("Account Number: " + acc.getAccountNumber().getValue());
            System.out.println("Balance: " + acc.getBalance().getAmount());
        });
    }
}
