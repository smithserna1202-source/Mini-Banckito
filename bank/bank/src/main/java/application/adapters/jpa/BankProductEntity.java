package application.adapters.jpa;
import application.domain.models.BankProduct;
import application.domain.enums.ProductType;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="bank_products")
public class BankProductEntity {
 @Id private String id;
 @Column(nullable=false) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ProductType type;
 @Column(length=2000) private String description;
 @Column(precision=9,scale=4) private BigDecimal interestRate;
 @Column(nullable=false) private boolean active;
 protected BankProductEntity(){}
 public BankProductEntity(BankProduct p){id=p.getId();name=p.getName();type=p.getType();description=p.getDescription();interestRate=p.getInterestRate();active=p.isActive();}
 public String getId(){return id;} public String getName(){return name;} public ProductType getType(){return type;}
 public String getDescription(){return description;} public BigDecimal getInterestRate(){return interestRate;} public boolean isActive(){return active;}
}
