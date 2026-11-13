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
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/loans")
public class LoanController {
 private final LoanService loans;private final LoanRepository repository;
 public LoanController(LoanService l,LoanRepository r){loans=l;repository=r;}
 public record RequestLoan(@NotBlank String id,@NotBlank String applicantId,@NotBlank String type,@NotNull @DecimalMin("0.01") BigDecimal amount,@Min(1) int termMonths,@NotBlank String destinationAccount){}
 public record Decision(@NotBlank String analystId,@NotNull Role role,BigDecimal approvedAmount,BigDecimal interestRate){}
 @PostMapping public Map<String,String> request(@Valid @RequestBody RequestLoan r){
  Loan l=new Loan(r.id(),r.applicantId(),r.type(),Money.of(r.amount().toPlainString()),r.termMonths(),r.destinationAccount());loans.request(l);
  return Map.of("loanId",l.getId(),"status",l.getStatus().name());
 }
 @PostMapping("/{id}/approve") public Map<String,String> approve(@PathVariable String id,@Valid @RequestBody Decision d){
  if(d.approvedAmount()==null||d.interestRate()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Approved amount and interest rate are required");
  loans.approve(id,d.analystId(),d.role(),Money.of(d.approvedAmount().toPlainString()),d.interestRate());
  return Map.of("loanId",id,"status",repository.findById(id).orElseThrow().getStatus().name());
 }
 @PostMapping("/{id}/reject") public Map<String,String> reject(@PathVariable String id,@RequestParam String analystId,@RequestParam Role role){
  loans.reject(id,analystId,role);return Map.of("loanId",id,"status","REJECTED");
 }
}