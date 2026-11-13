package application.domain.services;
import application.domain.models.AuditLogEntry;
import application.domain.ports.AuditLogRepository;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
public class AuditService {
 private final AuditLogRepository repository;
 public AuditService(AuditLogRepository repository){this.repository=Objects.requireNonNull(repository);}
 public void record(String operation,String userId,String role,String productId,Map<String,Object> details){
  repository.append(new AuditLogEntry(UUID.randomUUID().toString(),operation,userId,role,productId,details));
 }
}