package application.api;
import application.domain.enums.Role;
import application.domain.models.Loan;
import application.domain.ports.LoanRepository;
import application.domain.services.LoanService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/loans")
public class LoanController {
 private final LoanService loans;private final LoanRepository repository;
 public LoanController(LoanService l,LoanRepository r){loans=l;repository=r;}
 public record RequestLoan(@NotBlank String id,@NotBlank String type,@NotNull @DecimalMin("0.01") BigDecimal amount,@Min(1) int termMonths,@NotBlank String destinationAccount){}
 public record Decision(@NotNull @DecimalMin("0.01") BigDecimal approvedAmount,@NotNull @DecimalMin("0.00") BigDecimal interestRate){}
 @PostMapping public Map<String,String> request(@Valid @RequestBody RequestLoan r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);if(role!=Role.NATURAL_PERSON_CUSTOMER&&role!=Role.COMPANY_CUSTOMER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only customers may request loans");
  String applicant=AuthenticatedActor.id(actor);
  Loan l=new Loan(r.id(),applicant,r.type(),Money.of(r.amount().toPlainString()),r.termMonths(),r.destinationAccount());loans.request(l);
  return Map.of("loanId",l.getId(),"status",l.getStatus().name());
 }
 @PostMapping("/{id}/approve") public Map<String,String> approve(@PathVariable String id,@Valid @RequestBody Decision d,Authentication actor){
  Role role=AuthenticatedActor.role(actor);loans.approve(id,AuthenticatedActor.id(actor),role,Money.of(d.approvedAmount().toPlainString()),d.interestRate());
  return Map.of("loanId",id,"status",repository.findById(id).orElseThrow().getStatus().name());
 }
 @PostMapping("/{id}/reject") public Map<String,String> reject(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);loans.reject(id,AuthenticatedActor.id(actor),role);
  return Map.of("loanId",id,"status","REJECTED");
 }
 @PostMapping("/{id}/disburse") public Map<String,String> disburse(@PathVariable String id,Authentication actor){
  if(AuthenticatedActor.role(actor)!=Role.INTERNAL_ANALYST)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only an analyst can disburse a loan");
  loans.disburse(id);return Map.of("loanId",id,"status","DISBURSED");
 }
}