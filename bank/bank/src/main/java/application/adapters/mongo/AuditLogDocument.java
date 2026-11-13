package application.adapters.mongo;
import application.domain.models.AuditLogEntry;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="bank_audit_log")
public class AuditLogDocument {
 @Id private String id; private String operationType; private Instant occurredAt; private String userId; private String role; private String affectedProductId; private Map<String,Object> details;
 protected AuditLogDocument(){}
 public AuditLogDocument(AuditLogEntry entry){
  this.id=entry.getId();this.operationType=entry.getOperationType();this.occurredAt=entry.getOccurredAt();
  this.userId=entry.getUserId();this.role=entry.getRole();this.affectedProductId=entry.getAffectedProductId();this.details=entry.getDetails();
 }
 public String getId(){return id;} public String getOperationType(){return operationType;} public Instant getOccurredAt(){return occurredAt;}
 public String getUserId(){return userId;} public String getRole(){return role;} public String getAffectedProductId(){return affectedProductId;}
 public Map<String,Object> getDetails(){return details;}
}