package application.api;
import application.domain.models.ScheduledTransfer;
import application.domain.services.CancelScheduledTransferService;
import application.domain.services.ScheduleTransferService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/scheduled-transfers")
public class ScheduledTransferController {
 private final ScheduleTransferService scheduler;private final CancelScheduledTransferService canceller;
 public ScheduledTransferController(ScheduleTransferService s,CancelScheduledTransferService c){scheduler=s;canceller=c;}
 public record ScheduleRequest(@NotBlank String sourceAccount,@NotBlank String destinationAccount,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull Instant scheduledAt,@NotBlank String creatorId,@NotBlank String role){}
 @PostMapping public Map<String,String> schedule(@Valid @RequestBody ScheduleRequest r){
  ScheduledTransfer t=scheduler.schedule(r.sourceAccount(),r.destinationAccount(),Money.of(r.amount().toPlainString()),r.scheduledAt(),r.creatorId(),r.role());
  return Map.of("transferId",t.getId(),"status",t.getStatus().name(),"scheduledAt",t.getScheduledAt().toString());
 }
 @PostMapping("/{id}/cancel") public Map<String,String> cancel(@PathVariable String id,@RequestParam String requesterId,@RequestParam String role){canceller.cancel(id,requesterId,role);return Map.of("transferId",id,"status","CANCELLED");}
}