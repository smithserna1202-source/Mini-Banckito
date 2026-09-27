package application.domain.ports;

import application.domain.models.ScheduledTransfer;
import java.util.Optional;

public interface ScheduledTransferRepository {
    void save(ScheduledTransfer scheduledTransfer);
    Optional<ScheduledTransfer> findById(String id);
}
