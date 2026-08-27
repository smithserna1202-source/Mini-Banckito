package application.domain.ports;

import application.domain.models.BankAccount;
import java.util.Optional;

public interface BankAccountRepository {
    void save(BankAccount account);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
