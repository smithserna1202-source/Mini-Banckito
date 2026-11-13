package application.adapters;

import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryBankAccountRepository implements BankAccountRepository {
    private final ConcurrentMap<String, BankAccount> accounts = new ConcurrentHashMap<>();
    @Override public void save(BankAccount account) { accounts.put(account.getAccountNumber().getValue(), account); }
    @Override public Optional<BankAccount> findByAccountNumber(String accountNumber) { return Optional.ofNullable(accounts.get(accountNumber)); }
}
