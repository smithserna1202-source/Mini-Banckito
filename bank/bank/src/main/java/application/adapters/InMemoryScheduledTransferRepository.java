package application.adapters;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.ScheduledTransferRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class InMemoryScheduledTransferRepository implements ScheduledTransferRepository {
 private final ConcurrentMap<String,ScheduledTransfer> transfers=new ConcurrentHashMap<>();
 @Override public void save(ScheduledTransfer transfer){transfers.put(transfer.getId(),transfer);}
 @Override public Optional<ScheduledTransfer> findById(String id){return Optional.ofNullable(transfers.get(id));}
 @Override public List<ScheduledTransfer> findAll(){return List.copyOf(transfers.values());}
}
