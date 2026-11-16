package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.CompanyRepository;
import application.domain.ports.CustomerRepository;
import java.util.Objects;

public class CreateAccountService {
    private final BankAccountRepository repository;
    private final CustomerRepository customers;
    private final CompanyRepository companies;

    public CreateAccountService(BankAccountRepository repository) {
        this(repository, null, null);
    }

    public CreateAccountService(BankAccountRepository repository, CustomerRepository customers,
                                CompanyRepository companies) {
        this.repository = Objects.requireNonNull(repository);
        this.customers = customers;
        this.companies = companies;
    }

    public void createAccount(BankAccount account) {
        Objects.requireNonNull(account, "Account is required");
        if (repository.findByAccountNumber(account.getAccountNumber().getValue()).isPresent()) {
            throw new IllegalArgumentException("Account number already exists");
        }
        if (customers != null && companies != null) {
            boolean activeNaturalPerson = customers.findById(account.getOwnerId())
                .filter(application.domain.models.Customer::isActive).isPresent();
            boolean activeCompany = companies.findById(account.getOwnerId())
                .filter(application.domain.models.Company::isActive).isPresent();
            if (!activeNaturalPerson && !activeCompany) {
                throw new IllegalArgumentException("Account owner must be an existing active customer or company");
            }
        }
        repository.save(account);
    }
}
