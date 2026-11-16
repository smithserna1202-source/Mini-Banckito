package application.api;
import application.domain.enums.AccountType;
import application.adapters.jpa.SpringDataCommercialCustomerAssignmentRepository;
import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.TransactionRepository;
import application.domain.models.Transaction;
import application.domain.enums.TransactionType;
import java.util.UUID;
import application.domain.services.*;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
@RestController @RequestMapping("/api/accounts")
public class AccountController {
 private final BankAccountRepository accounts;private final CreateAccountService create;private final DepositService deposits;private final WithdrawalService withdrawals;private final GetAccountBalanceService balances;private final AuditService audit;private final AuthorizationService auth;private final TransactionRepository transactions;private final SpringDataCommercialCustomerAssignmentRepository assignments;
 public AccountController(BankAccountRepository a,CreateAccountService c,DepositService d,WithdrawalService w,GetAccountBalanceService b,AuditService audit,AuthorizationService auth,TransactionRepository transactions,SpringDataCommercialCustomerAssignmentRepository assignments){accounts=a;create=c;deposits=d;withdrawals=w;balances=b;this.audit=audit;this.auth=auth;this.transactions=transactions;this.assignments=assignments;}
 public record CreateAccountRequest(@NotBlank String accountNumber,@NotBlank String ownerId,@NotNull AccountType type,@NotNull @DecimalMin("0.00") BigDecimal initialBalance,@Pattern(regexp="[A-Z]{3}") String currency){}
 public record AmountRequest(@NotNull @DecimalMin(value="0.01") BigDecimal amount){}

 @GetMapping public List<Map<String,Object>> list(Authentication actor){
  Role role=AuthenticatedActor.role(actor);String requester=AuthenticatedActor.id(actor);
  List<BankAccount> visible;
  if(role==Role.NATURAL_PERSON_CUSTOMER){
   visible=accounts.findAll().stream().filter(a->a.getOwnerId().equals(requester)).toList();
  } else if(role==Role.COMPANY_CUSTOMER||role==Role.COMPANY_OPERATIONS_EMPLOYEE||role==Role.COMPANY_SUPERVISOR){
   String companyId=AuthenticatedActor.companyId(actor);
   visible=accounts.findAll().stream().filter(a->a.getOwnerId().equals(companyId)).toList();
  } else if(role==Role.COMMERCIAL_EMPLOYEE){
   Set<String> customerIds=assignments.findByEmployeeUsernameAndActiveTrue(requester).stream()
    .map(application.adapters.jpa.CommercialCustomerAssignmentEntity::getCustomerId).collect(Collectors.toSet());
   visible=accounts.findAll().stream().filter(a->customerIds.contains(a.getOwnerId())).toList();
  } else if(role==Role.TELLER||role==Role.INTERNAL_ANALYST||role==Role.ADMINISTRATOR){
   visible=accounts.findAll();
  } else throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Account listing is not allowed");
  return visible.stream().map(a->Map.<String,Object>of(
   "accountNumber",a.getAccountNumber().getValue(),"ownerId",a.getOwnerId(),
   "type",a.getType().name(),"balance",a.getBalance().toString(),
   "currency",a.getCurrency(),"status",a.getStatus().name(),
   "openedAt",a.getOpenedAt().toString())).toList();
 }
 @Transactional
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateAccountRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);
  if(role==Role.COMMERCIAL_EMPLOYEE){
   if(assignments.findByEmployeeUsernameAndCustomerIdAndActiveTrue(AuthenticatedActor.id(actor),r.ownerId()).isEmpty())
    throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Commercial employee may open accounts only for assigned customers");
   if(r.initialBalance().signum()!=0)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Commercial employee cannot set an opening balance");
  } else auth.require(role,AuthorizationService.Action.OPEN_ACCOUNT);
  BankAccount a=new BankAccount(new AccountNumber(r.accountNumber()),new Money(r.initialBalance()),r.ownerId(),r.type(),r.currency()==null?"COP":r.currency());
  try{create.createAccount(a);}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,ex.getMessage());}
  audit.record("ACCOUNT_OPENED",AuthenticatedActor.id(actor),role.name(),r.accountNumber(),Map.of("type",r.type().name()));
  return Map.of("accountNumber",r.accountNumber(),"ownerId",r.ownerId(),"type",r.type(),"balance",a.getBalance().toString(),"status",a.getStatus());
 }
 @GetMapping("/{number}/balance") public Map<String,Object> balance(@PathVariable String number,Authentication actor){
  BankAccount a=find(number);Role role=AuthenticatedActor.role(actor);String id=AuthenticatedActor.id(actor);
  if(role==Role.TELLER||role==Role.ADMINISTRATOR||role==Role.INTERNAL_ANALYST)auth.require(role,AuthorizationService.Action.VIEW_ACCOUNT_STATUS);
  else if(role==Role.COMPANY_CUSTOMER||role==Role.COMPANY_OPERATIONS_EMPLOYEE||role==Role.COMPANY_SUPERVISOR){
   if(!a.getOwnerId().equals(AuthenticatedActor.companyId(actor)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Account belongs to another company");
  } else if(!a.getOwnerId().equals(id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You may only view your own account");
  return Map.of("accountNumber",number,"balance",balances.checkBalance(a).toString(),"currency",a.getCurrency(),"status",a.getStatus());
 }
 @Transactional
 @PostMapping("/{number}/deposit") public Map<String,Object> deposit(@PathVariable String number,@Valid @RequestBody AmountRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);auth.require(role,AuthorizationService.Action.DEPOSIT);
  BankAccount a=find(number);Money amount=Money.of(r.amount().toPlainString());deposits.deposit(a,amount);accounts.save(a);transactions.save(new Transaction(UUID.randomUUID().toString(),number,amount,TransactionType.DEPOSIT));
  audit.record("DEPOSIT",AuthenticatedActor.id(actor),role.name(),number,Map.of("amount",r.amount().toPlainString()));return Map.of("balance",a.getBalance().toString());
 }
 @Transactional
 @PostMapping("/{number}/withdraw") public Map<String,Object> withdraw(@PathVariable String number,@Valid @RequestBody AmountRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);auth.require(role,AuthorizationService.Action.WITHDRAW);
  BankAccount a=find(number);Money amount=Money.of(r.amount().toPlainString());withdrawals.withdraw(a,amount);accounts.save(a);transactions.save(new Transaction(UUID.randomUUID().toString(),number,amount,TransactionType.WITHDRAWAL));
  audit.record("WITHDRAWAL",AuthenticatedActor.id(actor),role.name(),number,Map.of("amount",r.amount().toPlainString()));return Map.of("balance",a.getBalance().toString());
 }
 private BankAccount find(String n){return accounts.findByAccountNumber(n).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));}
}