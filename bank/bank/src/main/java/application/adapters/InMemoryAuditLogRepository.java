package application.adapters;
import application.domain.models.AuditLogEntry;
import application.domain.ports.AuditLogRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class InMemoryAuditLogRepository implements AuditLogRepository {
 private final List<AuditLogEntry> entries=new CopyOnWriteArrayList<>();
 @Override public void append(AuditLogEntry entry){entries.add(entry);}
 @Override public List<AuditLogEntry> findAll(){return List.copyOf(entries);}
}