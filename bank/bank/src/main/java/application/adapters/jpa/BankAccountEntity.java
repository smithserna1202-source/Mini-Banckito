package application.adapters.jpa;
import application.domain.models.BankAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="bank_accounts")
public class BankAccountEntity {
 @Id @Column(length=10) private String accountNumber;
 @Column(nullable=false) private String ownerId;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private application.domain.enums.AccountType type;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal balance;
 @Column(nullable=false,length=3) private String currency;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private application.domain.enums.AccountStatus status;
 @Column(nullable=false) private Instant openedAt;
 protected BankAccountEntity(){}
 public BankAccountEntity(BankAccount a){accountNumber=a.getAccountNumber().getValue();ownerId=a.getOwnerId();type=a.getType();balance=a.getBalance().getAmountDecimal();currency=a.getCurrency();status=a.getStatus();openedAt=a.getOpenedAt();}
 public String getAccountNumber(){return accountNumber;} public String getOwnerId(){return ownerId;} public application.domain.enums.AccountType getType(){return type;}
 public BigDecimal getBalance(){return balance;} public String getCurrency(){return currency;} public application.domain.enums.AccountStatus getStatus(){return status;} public Instant getOpenedAt(){return openedAt;}
}