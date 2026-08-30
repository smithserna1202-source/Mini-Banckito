package application.adapters;

import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryBankAccountRepository implements BankAccountRepository {
    private final Map<String, BankAccount> accounts = new HashMap<>();

    @Override
    public void save(BankAccount account) {
        accounts.put(account.getAccountNumber(), account);
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }
}
