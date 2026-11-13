package application.domain.models;
import application.domain.enums.ProductType;
import java.math.BigDecimal;
import java.util.Objects;
public final class BankProduct {
 private final String id,name,description; private final ProductType type; private final BigDecimal interestRate; private boolean active=true;
 public BankProduct(String id,String name,ProductType type,String description,BigDecimal interestRate){
  this.id=Objects.requireNonNull(id);this.name=Objects.requireNonNull(name);this.type=Objects.requireNonNull(type);this.description=description==null?"":description;
  if(interestRate!=null&&interestRate.signum()<0)throw new IllegalArgumentException("Interest rate cannot be negative");this.interestRate=interestRate;
 }
 public String getId(){return id;} public String getName(){return name;} public ProductType getType(){return type;} public String getDescription(){return description;}
 public BigDecimal getInterestRate(){return interestRate;} public boolean isActive(){return active;} public void deactivate(){active=false;}
}