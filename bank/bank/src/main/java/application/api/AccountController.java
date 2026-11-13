package application.api;
import application.domain.enums.AccountType;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.services.*;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
 private final BankAccountRepository accounts;private final CreateAccountService create;private final DepositService deposits;private final WithdrawalService withdrawals;private final GetAccountBalanceService balances;private final AuditService audit;
 public AccountController(BankAccountRepository a,CreateAccountService c,DepositService d,WithdrawalService w,GetAccountBalanceService b,AuditService audit){accounts=a;create=c;deposits=d;withdrawals=w;balances=b;this.audit=audit;}
 public record CreateAccountRequest(@NotBlank String accountNumber,@NotBlank String ownerId,@NotNull AccountType type,@NotNull @DecimalMin("0.00") BigDecimal initialBalance,@Pattern(regexp="[A-Z]{3}") String currency){}
 public record AmountRequest(@NotNull @DecimalMin(value="0.01") BigDecimal amount,@NotBlank String userId,@NotBlank String role){}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateAccountRequest r){
  BankAccount a=new BankAccount(new AccountNumber(r.accountNumber()),new Money(r.initialBalance()),r.ownerId(),r.type(),r.currency()==null?"COP":r.currency());
  try{create.createAccount(a);}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,ex.getMessage());}
  audit.record("ACCOUNT_OPENED",r.ownerId(),"SYSTEM",r.accountNumber(),Map.of("type",r.type().name()));
  return Map.of("accountNumber",r.accountNumber(),"ownerId",r.ownerId(),"type",r.type(),"balance",a.getBalance().toString(),"status",a.getStatus());
 }
 @GetMapping("/{number}/balance") public Map<String,Object> balance(@PathVariable String number){BankAccount a=find(number);return Map.of("accountNumber",number,"balance",balances.checkBalance(a).toString(),"currency",a.getCurrency(),"status",a.getStatus());}
 @PostMapping("/{number}/deposit") public Map<String,Object> deposit(@PathVariable String number,@Valid @RequestBody AmountRequest r){
  BankAccount a=find(number);deposits.deposit(a,Money.of(r.amount().toPlainString()));accounts.save(a);audit.record("DEPOSIT",r.userId(),r.role(),number,Map.of("amount",r.amount().toPlainString()));return Map.of("balance",a.getBalance().toString());
 }
 @PostMapping("/{number}/withdraw") public Map<String,Object> withdraw(@PathVariable String number,@Valid @RequestBody AmountRequest r){
  BankAccount a=find(number);withdrawals.withdraw(a,Money.of(r.amount().toPlainString()));accounts.save(a);audit.record("WITHDRAWAL",r.userId(),r.role(),number,Map.of("amount",r.amount().toPlainString()));return Map.of("balance",a.getBalance().toString());
 }
 private BankAccount find(String n){return accounts.findByAccountNumber(n).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));}
}