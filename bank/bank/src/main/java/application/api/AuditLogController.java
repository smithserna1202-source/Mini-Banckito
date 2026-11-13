package application.api;
import application.domain.enums.Role;
import application.domain.ports.AuditLogRepository;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/audit")
public class AuditLogController {
 private final AuditLogRepository audit;
 public AuditLogController(AuditLogRepository audit){this.audit=audit;}
 @GetMapping public Object list(Authentication actor){
  Role role=AuthenticatedActor.role(actor);
  if(role!=Role.INTERNAL_ANALYST&&role!=Role.ADMINISTRATOR)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Audit access denied");
  return audit.findAll().stream().map(e->Map.of("id",e.getId(),"operation",e.getOperationType(),"occurredAt",e.getOccurredAt().toString(),"userId",e.getUserId(),"role",e.getRole(),"productId",e.getAffectedProductId()==null?"":e.getAffectedProductId(),"details",e.getDetails())).toList();
 }
}