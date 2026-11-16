package application.domain.valueobjects;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
/** Monetary value with decimal precision. */
public final class Money {
 private final BigDecimal amount;
 public Money(double amount){this(BigDecimal.valueOf(amount));}
 public Money(BigDecimal amount){
  Objects.requireNonNull(amount,"Amount is required");
  if(amount.signum()<0) throw new IllegalArgumentException("Amount cannot be negative");
  this.amount=amount.setScale(2,RoundingMode.HALF_EVEN);
 }
 public static Money of(String amount){return new Money(new BigDecimal(amount));}
 public static Money zero(){return new Money(BigDecimal.ZERO);}
 public double getAmount(){return amount.doubleValue();}
 public BigDecimal getAmountDecimal(){return amount;}
 public Money add(Money other){return new Money(amount.add(other.amount));}
 public Money subtract(Money other){if(amount.compareTo(other.amount)<0)throw new IllegalArgumentException("Insufficient funds");return new Money(amount.subtract(other.amount));}
 public boolean isPositive(){return amount.signum()>0;}
 public boolean isGreaterThan(Money other){return amount.compareTo(other.amount)>0;}
 @Override public boolean equals(Object o){return o instanceof Money m && amount.compareTo(m.amount)==0;}
 @Override public int hashCode(){return amount.stripTrailingZeros().hashCode();}
 @Override public String toString(){return amount.toPlainString();}
}