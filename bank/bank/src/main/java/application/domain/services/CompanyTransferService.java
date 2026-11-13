package application.domain.services;
import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.models.BankTransfer;
import application.domain.ports.BankAccountRepository;
import application.domain.valueobjects.Money;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class CompanyTransferService {
 private final BankAccountRepository accounts; private final AuthorizationService authorization; private final AuditService audit;
 private final ConcurrentMap<String,BankTransfer> transfers=new ConcurrentHashMap<>(); private final Money approvalThreshold;
 public CompanyTransferService(BankAccountRepository accounts,AuthorizationService authorization,AuditService audit,Money approvalThreshold){
  this.accounts=Objects.requireNonNull(accounts);this.authorization=Objects.requireNonNull(authorization);this.audit=Objects.requireNonNull(audit);
  if(approvalThreshold==null||!approvalThreshold.isPositive())throw new IllegalArgumentException("Approval threshold must be positive");this.approvalThreshold=approvalThreshold;
 }
 public BankTransfer create(String companyId,String sourceId,String destinationId,Money amount,String userId,Role role){
  authorization.require(role,AuthorizationService.Action.REQUEST_TRANSFER);
  BankAccount source=accounts.findByAccountNumber(sourceId).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  BankAccount target=accounts.findByAccountNumber(destinationId).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!source.getOwnerId().equals(companyId))throw new SecurityException("Employee cannot operate on another company account");
  if(source.getStatus()!=application.domain.enums.AccountStatus.ACTIVE||target.getStatus()!=application.domain.enums.AccountStatus.ACTIVE)throw new IllegalStateException("Both accounts must be active");
  boolean approval=amount.isGreaterThan(approvalThreshold);
  BankTransfer transfer=new BankTransfer(UUID.randomUUID().toString(),sourceId,destinationId,amount,userId,approval);
  transfers.put(transfer.getId(),transfer);
  if(!approval)execute(transfer);
  else audit.record("TRANSFER_PENDING_APPROVAL",userId,role.name(),transfer.getId(),Map.of("amount",amount.toString()));
  return transfer;
 }
 public void approve(String id,String supervisorId,Role role){
  authorization.require(role,AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
  BankTransfer transfer=require(id);transfer.expire();
  if(transfer.getStatus()==application.domain.enums.TransferStatus.EXPIRED){audit.record("TRANSFER_EXPIRED",supervisorId,role.name(),id,Map.of());return;}
  transfer.approve(supervisorId);execute(transfer);
 }
 public void reject(String id,String supervisorId,Role role){
  authorization.require(role,AuthorizationService.Action.APPROVE_HIGH_VALUE_TRANSFER);
  BankTransfer transfer=require(id);transfer.expire();
  if(transfer.getStatus()==application.domain.enums.TransferStatus.EXPIRED){audit.record("TRANSFER_EXPIRED",supervisorId,role.name(),id,Map.of());return;}
  transfer.reject(supervisorId);audit.record("TRANSFER_REJECTED",supervisorId,role.name(),id,Map.of());
 }
 public void expirePending(){
  transfers.values().forEach(t->{var before=t.getStatus();t.expire();if(before!=t.getStatus())audit.record("TRANSFER_EXPIRED","SYSTEM","SYSTEM",t.getId(),Map.of());});
 }
 private BankTransfer require(String id){BankTransfer t=transfers.get(id);if(t==null)throw new IllegalArgumentException("Transfer not found");return t;}
 private void execute(BankTransfer t){
  BankAccount source=accounts.findByAccountNumber(t.getSourceAccount()).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  BankAccount target=accounts.findByAccountNumber(t.getDestinationAccount()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  source.withdraw(t.getAmount());try{target.deposit(t.getAmount());}catch(RuntimeException e){source.deposit(t.getAmount());throw e;}
  t.execute();accounts.save(source);accounts.save(target);
  audit.record("TRANSFER_EXECUTED",t.getCreatorUserId(),"COMPANY",t.getId(),Map.of("amount",t.getAmount().toString(),"source",t.getSourceAccount(),"destination",t.getDestinationAccount()));
 }
}