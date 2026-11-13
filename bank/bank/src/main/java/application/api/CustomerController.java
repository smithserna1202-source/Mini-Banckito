package application.api;
import application.domain.enums.Role;
import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import application.domain.services.AuditService;
import application.domain.valueobjects.PhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
 private final CustomerRepository customers;private final AuditService audit;
 public CustomerController(CustomerRepository c,AuditService a){customers=c;audit=a;}
 public record CreateCustomerRequest(@NotBlank String name,@NotBlank String identification,@jakarta.validation.constraints.Email String email,@Pattern(regexp="\\d{7,15}") String phone,@NotNull LocalDate birthDate,String address){}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateCustomerRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);if(role!=Role.ADMINISTRATOR&&role!=Role.NATURAL_PERSON_CUSTOMER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Role cannot register a natural-person customer");
  String id=AuthenticatedActor.id(actor);
  Customer c=new Customer(id,r.name(),r.identification(),new application.domain.valueobjects.Email(r.email()),r.phone()==null?null:new PhoneNumber(r.phone()),r.address(),r.birthDate(),true);
  if(customers.findById(id).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Customer id already exists");
  if(customers.findByIdentification(r.identification()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Identification already exists");
  if(customers.findByEmail(r.email()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
  customers.save(c);audit.record("CUSTOMER_REGISTERED",id,role.name(),id,Map.of("identification",r.identification()));
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"active",c.isActive());
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);String requester=AuthenticatedActor.id(actor);
  if(!requester.equals(id)&&role!=Role.ADMINISTRATOR&&role!=Role.COMMERCIAL_EMPLOYEE)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You may only view your own profile");
  Customer c=customers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found"));
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"identification",c.getIdentification(),"active",c.isActive());
 }
}