package application.api;
import application.domain.enums.Role;
import application.domain.enums.TransactionType;
import application.domain.models.BankAccount;
import application.domain.models.Transaction;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.TransactionRepository;
import application.domain.services.AuditService;
import application.domain.services.AuthorizationService;
import application.domain.services.InternalTransferService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/transfers")
public class TransferController {
 private final BankAccountRepository accounts;private final TransactionRepository transactions;private final AuthorizationService auth;private final InternalTransferService transfers;private final AuditService audit;
 public TransferController(BankAccountRepository a,TransactionRepository t,AuthorizationService z,AuditService audit){accounts=a;transactions=t;auth=z;this.audit=audit;transfers=new InternalTransferService();}
 public record TransferRequest(@NotBlank String sourceAccount,@NotBlank String destinationAccount,@NotNull @DecimalMin("0.01") BigDecimal amount){}
 @PostMapping("/internal") public Map<String,Object> transfer(@Valid @RequestBody TransferRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);auth.require(role,AuthorizationService.Action.REQUEST_TRANSFER);
  String requester=AuthenticatedActor.id(actor);BankAccount source=find(r.sourceAccount()),target=find(r.destinationAccount());
  if(!source.getOwnerId().equals(requester))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Requester does not own the source account");
  Money amount=Money.of(r.amount().toPlainString());transfers.transfer(source,target,amount);accounts.save(source);accounts.save(target);
  transactions.save(new Transaction(UUID.randomUUID().toString(),r.sourceAccount(),amount,TransactionType.TRANSFER));
  transactions.save(new Transaction(UUID.randomUUID().toString(),r.destinationAccount(),amount,TransactionType.TRANSFER));
  audit.record("INTERNAL_TRANSFER",requester,role.name(),r.sourceAccount(),Map.of("destination",r.destinationAccount(),"amount",amount.toString()));
  return Map.of("status","EXECUTED","sourceBalance",source.getBalance().toString(),"destinationBalance",target.getBalance().toString());
 }
 private BankAccount find(String n){return accounts.findByAccountNumber(n).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found: "+n));}
}