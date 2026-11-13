package application.domain.services;
import application.domain.models.BankAccount;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.ScheduledTransferRepository;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
public class ScheduleTransferService {
 private final ScheduledTransferRepository transfers;private final BankAccountRepository accounts;private final AuditService audit;
 public ScheduleTransferService(ScheduledTransferRepository transfers,BankAccountRepository accounts,AuditService audit){this.transfers=Objects.requireNonNull(transfers);this.accounts=Objects.requireNonNull(accounts);this.audit=Objects.requireNonNull(audit);}
 public ScheduledTransfer schedule(String source,String destination,Money amount,Instant at,String creatorId,String role){
  BankAccount from=accounts.findByAccountNumber(source).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  BankAccount to=accounts.findByAccountNumber(destination).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!from.getOwnerId().equals(creatorId))throw new SecurityException("Only the owner may schedule a transfer");
  if(!from.getCurrency().equals(to.getCurrency()))throw new IllegalArgumentException("Account currencies must match");
  ScheduledTransfer transfer=new ScheduledTransfer(UUID.randomUUID().toString(),new AccountNumber(source),new AccountNumber(destination),amount,at,creatorId);
  transfers.save(transfer);audit.record("TRANSFER_SCHEDULED",creatorId,role,transfer.getId(),Map.of("amount",amount.toString(),"scheduledAt",at.toString()));return transfer;
 }
}