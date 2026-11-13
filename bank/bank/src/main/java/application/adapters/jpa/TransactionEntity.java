package application.adapters.jpa;
import application.domain.enums.TransactionType;
import application.domain.models.Transaction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="bank_transactions",indexes=@Index(name="idx_transaction_account_created",columnList="accountId,createdAt"))
public class TransactionEntity {
 @Id private String id;
 @Column(nullable=false) private String accountId;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private TransactionType type;
 @Column(nullable=false) private Instant createdAt;
 protected TransactionEntity(){}
 public TransactionEntity(Transaction t){id=t.getId();accountId=t.getAccountId();amount=t.getAmount().getAmountDecimal();type=t.getType();createdAt=Instant.now();}
 public String getId(){return id;} public String getAccountId(){return accountId;} public BigDecimal getAmount(){return amount;} public TransactionType getType(){return type;} public Instant getCreatedAt(){return createdAt;}
}
