package application.adapters.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBankTransferRepository extends JpaRepository<BankTransferEntity, String> {}
