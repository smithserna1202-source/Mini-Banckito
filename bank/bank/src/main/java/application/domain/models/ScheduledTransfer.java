package application.domain.models;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import java.time.Instant;
import java.util.Objects;
public final class ScheduledTransfer {
 public enum Status { SCHEDULED, EXECUTED, CANCELLED, FAILED }
 private final String id,creatorId; private final AccountNumber source,destination; private final Money amount; private final Instant scheduledAt,createdAt=Instant.now();
 private Status status=Status.SCHEDULED;
 public ScheduledTransfer(String id,AccountNumber source,AccountNumber destination,Money amount){this(id,source,destination,amount,Instant.now().plusSeconds(60),"SYSTEM");}
 public ScheduledTransfer(String id,AccountNumber source,AccountNumber destination,Money amount,Instant scheduledAt,String creatorId){
  if(id==null||id.isBlank())throw new IllegalArgumentException("Transfer id is required");
  this.source=Objects.requireNonNull(source);this.destination=Objects.requireNonNull(destination);
  if(source.getValue().equals(destination.getValue()))throw new IllegalArgumentException("Accounts must differ");
  if(amount==null||!amount.isPositive())throw new IllegalArgumentException("Amount must be positive");
  this.amount=amount;this.id=id;this.scheduledAt=Objects.requireNonNull(scheduledAt);this.creatorId=Objects.requireNonNull(creatorId);
  if(!scheduledAt.isAfter(Instant.now()))throw new IllegalArgumentException("Scheduled time must be in the future");
 }
 public void cancel(){if(status!=Status.SCHEDULED)throw new IllegalStateException("Only scheduled transfers can be cancelled");status=Status.CANCELLED;}
 public void markExecuted(){if(status!=Status.SCHEDULED)throw new IllegalStateException("Transfer is not scheduled");status=Status.EXECUTED;}
 public void markFailed(){if(status==Status.SCHEDULED)status=Status.FAILED;}
 public String getId(){return id;} public AccountNumber getSource(){return source;} public AccountNumber getDestination(){return destination;}
 public Money getAmount(){return amount;} public Instant getScheduledAt(){return scheduledAt;} public Instant getCreatedAt(){return createdAt;}
 public String getCreatorId(){return creatorId;} public Status getStatus(){return status;}
}