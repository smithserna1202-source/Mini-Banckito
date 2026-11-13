package application.adapters.mongo;
import application.domain.models.AuditLogEntry;
import application.domain.ports.AuditLogRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository
@Profile("mongodb")
public class MongoAuditLogAdapter implements AuditLogRepository {
 private final SpringDataAuditLogRepository repository;
 public MongoAuditLogAdapter(SpringDataAuditLogRepository repository){this.repository=Objects.requireNonNull(repository);}
 @Override public void append(AuditLogEntry entry){repository.insert(new AuditLogDocument(entry));}
 @Override public List<AuditLogEntry> findAll(){
  // Audit records are persisted as append-only documents; this adapter intentionally exposes no update/delete operation.
  return repository.findAll().stream().map(d->new AuditLogEntry(d.getId(),d.getOperationType(),d.getUserId(),d.getRole(),d.getAffectedProductId(),d.getDetails())).toList();
 }
}