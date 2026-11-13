package application.api;
import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import application.domain.services.AuditService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
 private final CustomerRepository customers;private final AuditService audit;
 public CustomerController(CustomerRepository c,AuditService a){customers=c;audit=a;}
 public record CreateCustomerRequest(@NotBlank String id,@NotBlank String name,@NotBlank String identification,@Email String email,@Pattern(regexp="\\d{7,15}") String phone,LocalDate birthDate,String address){}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateCustomerRequest r){
  Customer c=new Customer(r.id(),r.name(),r.identification(),new Email(r.email()),r.phone()==null?null:new PhoneNumber(r.phone()),r.address(),r.birthDate(),true);
  if(customers.findById(r.id()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Customer id already exists");
  customers.save(c);audit.record("CUSTOMER_REGISTERED",r.id(),"SYSTEM",r.id(),Map.of("identification",r.identification()));
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"active",c.isActive());
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable String id){
  Customer c=customers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found"));
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"identification",c.getIdentification(),"active",c.isActive());
 }
}