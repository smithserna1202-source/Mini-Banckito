package application.api;
import application.domain.enums.Role;
import application.domain.models.ScheduledTransfer;
import application.domain.services.CancelScheduledTransferService;
import application.domain.services.ScheduleTransferService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/scheduled-transfers")
public class ScheduledTransferController {
 private final ScheduleTransferService scheduler;private final CancelScheduledTransferService canceller;
 public ScheduledTransferController(ScheduleTransferService s,CancelScheduledTransferService c){scheduler=s;canceller=c;}
 public record ScheduleRequest(@NotBlank String sourceAccount,@NotBlank String destinationAccount,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull Instant scheduledAt){}
 @PostMapping public Map<String,String> schedule(@Valid @RequestBody ScheduleRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);if(role!=Role.NATURAL_PERSON_CUSTOMER&&role!=Role.COMPANY_CUSTOMER&&role!=Role.COMPANY_OPERATIONS_EMPLOYEE)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Role cannot schedule transfers");
  String id=AuthenticatedActor.id(actor);
  ScheduledTransfer t=scheduler.schedule(r.sourceAccount(),r.destinationAccount(),Money.of(r.amount().toPlainString()),r.scheduledAt(),id,role.name());
  return Map.of("transferId",t.getId(),"status",t.getStatus().name(),"scheduledAt",t.getScheduledAt().toString());
 }
 @PostMapping("/{id}/cancel") public Map<String,String> cancel(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);canceller.cancel(id,AuthenticatedActor.id(actor),role.name());return Map.of("transferId",id,"status","CANCELLED");
 }
}