package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.CompanyRepository;
import application.domain.ports.CustomerRepository;
import application.domain.ports.BankProductRepository;
import application.domain.enums.AccountType;
import application.domain.enums.ProductType;
import java.util.Objects;

public class CreateAccountService {
    private final BankAccountRepository repository;
    private final CustomerRepository customers;
    private final CompanyRepository companies;
    private final BankProductRepository products;

    public CreateAccountService(BankAccountRepository repository) {
        this(repository, null, null, null);
    }

    public CreateAccountService(BankAccountRepository repository, CustomerRepository customers,
                                CompanyRepository companies) {
        this(repository, customers, companies, null);
    }

    public CreateAccountService(BankAccountRepository repository, CustomerRepository customers,
                                CompanyRepository companies, BankProductRepository products) {
        this.repository = Objects.requireNonNull(repository);
        this.customers = customers;
        this.companies = companies;
        this.products = products;
    }

    public void createAccount(BankAccount account) {
        Objects.requireNonNull(account, "Account is required");
        if (repository.findByAccountNumber(account.getAccountNumber().getValue()).isPresent()) {
            throw new IllegalArgumentException("Account number already exists");
        }
        if (products != null) {
            ProductType requiredType = switch (account.getType()) {
                case SAVINGS -> ProductType.SAVINGS_ACCOUNT;
                case CHECKING -> ProductType.CHECKING_ACCOUNT;
                case PERSONAL -> ProductType.PERSONAL_ACCOUNT;
                case BUSINESS -> ProductType.BUSINESS_ACCOUNT;
            };
            boolean offered = products.findAllActive().stream().anyMatch(p -> p.getType() == requiredType);
            if (!offered) throw new IllegalArgumentException("Account type is not available in the active product catalog");
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
