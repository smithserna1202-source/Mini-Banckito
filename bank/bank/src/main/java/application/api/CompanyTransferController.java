package application.api;
import application.domain.enums.Role;
import application.domain.models.BankTransfer;
import application.domain.services.CompanyTransferService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/company-transfers")
public class CompanyTransferController {
 private final CompanyTransferService transfers;
 public CompanyTransferController(CompanyTransferService transfers){this.transfers=transfers;}
 public record CreateRequest(@NotBlank String sourceAccount,@NotBlank String destinationAccount,@NotNull @DecimalMin("0.01") BigDecimal amount){}
 @PostMapping public Map<String,String> create(@Valid @RequestBody CreateRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);if(role!=Role.COMPANY_OPERATIONS_EMPLOYEE&&role!=Role.COMPANY_CUSTOMER&&role!=Role.ADMINISTRATOR)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Role cannot create company transfers");
  String actorId=AuthenticatedActor.id(actor);String companyId=role==Role.ADMINISTRATOR?actorId: "COMPANY001";
  BankTransfer t=transfers.create(companyId,r.sourceAccount(),r.destinationAccount(),Money.of(r.amount().toPlainString()),actorId,role);
  return Map.of("transferId",t.getId(),"status",t.getStatus().name());
 }
 @PostMapping("/{id}/approve") public Map<String,String> approve(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);transfers.approve(id,AuthenticatedActor.id(actor),role);return Map.of("transferId",id,"status","APPROVED_OR_EXPIRED");
 }
 @PostMapping("/{id}/reject") public Map<String,String> reject(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);transfers.reject(id,AuthenticatedActor.id(actor),role);return Map.of("transferId",id,"status","REJECTED_OR_EXPIRED");
 }
 @PostMapping("/expire-pending") public Map<String,String> expire(Authentication actor){
  if(AuthenticatedActor.role(actor)!=Role.ADMINISTRATOR)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator role required");
  transfers.expirePending();return Map.of("status","EXPIRATION_CHECK_COMPLETED");
 }
}