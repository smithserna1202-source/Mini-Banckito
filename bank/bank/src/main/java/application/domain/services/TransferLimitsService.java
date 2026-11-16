package application.domain.services;
import application.domain.valueobjects.Money;
import java.util.Objects;
/** Stateless validation of a per-operation and daily transfer cap. */
public class TransferLimitsService {
 private final Money perTransactionLimit,dailyLimit;
 public TransferLimitsService(Money perTransactionLimit,Money dailyLimit){
  this.perTransactionLimit=Objects.requireNonNull(perTransactionLimit);this.dailyLimit=Objects.requireNonNull(dailyLimit);
  if(!perTransactionLimit.isPositive()||!dailyLimit.isPositive())throw new IllegalArgumentException("Limits must be positive");
  if(perTransactionLimit.isGreaterThan(dailyLimit))throw new IllegalArgumentException("Per-transaction limit cannot exceed daily limit");
 }
 public void validate(Money amount,Money alreadyTransferredToday){
  if(amount==null||!amount.isPositive())throw new IllegalArgumentException("Transfer amount must be positive");
  if(amount.isGreaterThan(perTransactionLimit))throw new IllegalArgumentException("Per-transaction limit exceeded");
  if(alreadyTransferredToday==null||alreadyTransferredToday.getAmountDecimal().add(amount.getAmountDecimal()).compareTo(dailyLimit.getAmountDecimal())>0)throw new IllegalArgumentException("Daily transfer limit exceeded");
 }
 public Money getPerTransactionLimit(){return perTransactionLimit;}public Money getDailyLimit(){return dailyLimit;}
}