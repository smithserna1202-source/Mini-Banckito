package application.infrastructure;

import application.adapters.InMemoryBankAccountRepository;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.ports.BankAccountRepository;

public class ConsoleApplication {
    public static void main(String[] args) {
        BankAccountRepository repository = new InMemoryBankAccountRepository();

        Customer customer = new Customer("1", "Manuela Serna", "manuela@example.com");
        BankAccount account = new BankAccount("ACC123", 500000.0, customer.getId());

        repository.save(account);

        repository.findByAccountNumber("ACC123").ifPresent(acc -> {
            System.out.println("Cuenta encontrada!");
            System.out.println("Número de Cuenta: " + acc.getAccountNumber());
            System.out.println("Saldo: " + acc.getBalance());
        });
    }
}
