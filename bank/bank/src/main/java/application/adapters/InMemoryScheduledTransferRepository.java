package application.adapters;

import application.domain.models.ScheduledTransfer;
import application.domain.ports.ScheduledTransferRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryScheduledTransferRepository implements ScheduledTransferRepository {
    private final Map<String, ScheduledTransfer> transfers = new HashMap<>();

    @Override
    public void save(ScheduledTransfer scheduledTransfer) {
        transfers.put(scheduledTransfer.toString(), scheduledTransfer);
    }

    @Override
    public Optional<ScheduledTransfer> findById(String id) {
        return Optional.ofNullable(transfers.get(id));
    }
}
