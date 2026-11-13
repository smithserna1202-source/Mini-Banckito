package application.domain.models;
import application.domain.enums.TransferStatus;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Objects;
public final class BankTransfer {
 private final String id,sourceAccount,destinationAccount,creatorUserId; private final Money amount;
 private final boolean requiresApproval; private final Instant createdAt=Instant.now();
 private TransferStatus status; private String approverUserId; private Instant approvedAt;
 public BankTransfer(String id,String source,String destination,Money amount,String creator,boolean requiresApproval){
  if(id==null||id.isBlank())throw new IllegalArgumentException("Transfer id is required");
  if(source==null||destination==null||source.equals(destination))throw new IllegalArgumentException("Source and destination must differ");
  if(amount==null||!amount.isPositive())throw new IllegalArgumentException("Amount must be positive");
  this.id=id;this.sourceAccount=source;this.destinationAccount=destination;this.amount=amount;this.creatorUserId=Objects.requireNonNull(creator);
  this.requiresApproval=requiresApproval;this.status=requiresApproval?TransferStatus.PENDING_APPROVAL:TransferStatus.APPROVED;
 }
 public void approve(String supervisor){requirePending();approverUserId=Objects.requireNonNull(supervisor);approvedAt=Instant.now();status=TransferStatus.APPROVED;}
 public void reject(String supervisor){requirePending();approverUserId=Objects.requireNonNull(supervisor);approvedAt=Instant.now();status=TransferStatus.REJECTED;}
 public void execute(){if(status!=TransferStatus.APPROVED)throw new IllegalStateException("Only approved transfers can execute");status=TransferStatus.EXECUTED;}
 public void expire(){if(status==TransferStatus.PENDING_APPROVAL&&Instant.now().isAfter(createdAt.plusSeconds(3600)))status=TransferStatus.EXPIRED;}
 private void requirePending(){if(!requiresApproval||status!=TransferStatus.PENDING_APPROVAL)throw new IllegalStateException("Transfer is not awaiting approval");}
 public String getId(){return id;} public String getSourceAccount(){return sourceAccount;} public String getDestinationAccount(){return destinationAccount;}
 public Money getAmount(){return amount;} public String getCreatorUserId(){return creatorUserId;} public Instant getCreatedAt(){return createdAt;}
 public boolean isRequiresApproval(){return requiresApproval;} public TransferStatus getStatus(){return status;} public String getApproverUserId(){return approverUserId;} public Instant getApprovedAt(){return approvedAt;}
}