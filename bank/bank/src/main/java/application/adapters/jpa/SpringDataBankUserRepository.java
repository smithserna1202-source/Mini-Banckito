package application.adapters.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBankUserRepository extends JpaRepository<BankUserEntity, String> {
    boolean existsByIdentification(String identification);
    boolean existsByEmail(String email);
}
