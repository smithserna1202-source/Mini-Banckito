package application.domain.ports;

import application.domain.models.BankAccount;
import java.util.Optional;
import java.util.List;

public interface BankAccountRepository {
    void save(BankAccount account);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    List<BankAccount> findAll();
}
