package application.domain.ports;

import application.domain.models.BankTransfer;
import java.util.List;
import java.util.Optional;

public interface BankTransferRepository {
    void save(BankTransfer transfer);
    Optional<BankTransfer> findById(String id);
    List<BankTransfer> findAll();
}
