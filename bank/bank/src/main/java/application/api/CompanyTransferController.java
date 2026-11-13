package application.api;
import application.domain.enums.Role;
import application.domain.models.BankTransfer;
import application.domain.services.CompanyTransferService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/company-transfers")
public class CompanyTransferController {
 private final CompanyTransferService transfers;
 public CompanyTransferController(CompanyTransferService transfers){this.transfers=transfers;}
 public record CreateRequest(@NotBlank String companyId,@NotBlank String sourceAccount,@NotBlank String destinationAccount,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotBlank String userId,@NotNull Role role){}
 public record ApprovalRequest(@NotBlank String supervisorId,@NotNull Role role){}
 @PostMapping public Map<String,String> create(@Valid @RequestBody CreateRequest r){
  BankTransfer t=transfers.create(r.companyId(),r.sourceAccount(),r.destinationAccount(),Money.of(r.amount().toPlainString()),r.userId(),r.role());
  return Map.of("transferId",t.getId(),"status",t.getStatus().name());
 }
 @PostMapping("/{id}/approve") public Map<String,String> approve(@PathVariable String id,@Valid @RequestBody ApprovalRequest r){transfers.approve(id,r.supervisorId(),r.role());return Map.of("transferId",id,"status","APPROVED_OR_EXPIRED");}
 @PostMapping("/{id}/reject") public Map<String,String> reject(@PathVariable String id,@Valid @RequestBody ApprovalRequest r){transfers.reject(id,r.supervisorId(),r.role());return Map.of("transferId",id,"status","REJECTED_OR_EXPIRED");}
 @PostMapping("/expire-pending") public Map<String,String> expire(){transfers.expirePending();return Map.of("status","EXPIRATION_CHECK_COMPLETED");}
}