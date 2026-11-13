package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.ScheduledTransferRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.springframework.scheduling.annotation.Scheduled;
public class ExecuteScheduledTransfersService {
 private final ScheduledTransferRepository transfers;private final BankAccountRepository accounts;private final AuditService audit;
 public ExecuteScheduledTransfersService(ScheduledTransferRepository transfers,BankAccountRepository accounts,AuditService audit){this.transfers=Objects.requireNonNull(transfers);this.accounts=Objects.requireNonNull(accounts);this.audit=Objects.requireNonNull(audit);}
 @Scheduled(fixedDelayString="${bank.transfers.scheduler-delay-ms:30000}")
 public void executeAllDue(){for(ScheduledTransfer t:transfers.findAll())executeDue(t);}
 public void executeDue(ScheduledTransfer transfer){
  if(transfer.getStatus()!=ScheduledTransfer.Status.SCHEDULED||transfer.getScheduledAt().isAfter(Instant.now()))return;
  BankAccount from=accounts.findByAccountNumber(transfer.getSource().getValue()).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  BankAccount to=accounts.findByAccountNumber(transfer.getDestination().getValue()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!from.getCurrency().equals(to.getCurrency()))throw new IllegalArgumentException("Account currencies must match");
  from.withdraw(transfer.getAmount());try{to.deposit(transfer.getAmount());}catch(RuntimeException ex){from.deposit(transfer.getAmount());throw ex;}
  transfer.markExecuted();accounts.save(from);accounts.save(to);transfers.save(transfer);
  audit.record("SCHEDULED_TRANSFER_EXECUTED",transfer.getCreatorId(),"SYSTEM",transfer.getId(),Map.of("amount",transfer.getAmount().toString()));
 }
}
