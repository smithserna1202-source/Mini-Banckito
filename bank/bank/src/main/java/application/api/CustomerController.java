package application.api;
import application.domain.enums.Role;
import application.adapters.jpa.SpringDataCommercialCustomerAssignmentRepository;
import application.domain.models.Customer;
import application.domain.ports.CustomerRepository;
import application.domain.ports.CompanyRepository;
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
 private final CustomerRepository customers;private final CompanyRepository companies;private final AuditService audit;private final SpringDataCommercialCustomerAssignmentRepository assignments;
 public CustomerController(CustomerRepository c,CompanyRepository companies,AuditService a,SpringDataCommercialCustomerAssignmentRepository assignments){customers=c;this.companies=companies;audit=a;this.assignments=assignments;}
 public record CreateCustomerRequest(@NotBlank String name,@NotBlank String identification,@NotBlank @jakarta.validation.constraints.Email String email,@NotBlank @Pattern(regexp="\\d{7,15}") String phone,@NotNull LocalDate birthDate,@NotBlank String address,String customerId){}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateCustomerRequest r,Authentication actor){
  Role role=AuthenticatedActor.role(actor);
  if(role!=Role.ADMINISTRATOR&&role!=Role.TELLER&&role!=Role.NATURAL_PERSON_CUSTOMER)
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Role cannot register a natural-person customer");
  String id;
  if(role==Role.NATURAL_PERSON_CUSTOMER){
   id=AuthenticatedActor.id(actor);
   if(r.customerId()!=null&&!r.customerId().equals(id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Customer users can register only their own profile");
  } else {
   if(r.customerId()==null||r.customerId().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Customer id is required for staff registration");
   id=r.customerId();
  }
  Customer c=new Customer(id,r.name(),r.identification(),new application.domain.valueobjects.Email(r.email()),new PhoneNumber(r.phone()),r.address(),r.birthDate(),true);
  if(customers.findById(id).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Customer id already exists");
  if(customers.findByIdentification(r.identification()).isPresent()||companies.findByNit(r.identification()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Identification already exists in the bank");
  if(customers.findByEmail(r.email()).isPresent()||companies.findByEmail(r.email()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
  if(customers.findByPhone(r.phone()).isPresent()||companies.findByPhone(r.phone()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Phone number already exists");
  customers.save(c);audit.record("CUSTOMER_REGISTERED",id,role.name(),id,Map.of("identification",r.identification()));
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"active",c.isActive());
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);String requester=AuthenticatedActor.id(actor);
  if(!requester.equals(id)&&role!=Role.ADMINISTRATOR&&role!=Role.INTERNAL_ANALYST&&role!=Role.TELLER){
   boolean assigned=role==Role.COMMERCIAL_EMPLOYEE
    &&assignments.findByEmployeeUsernameAndCustomerIdAndActiveTrue(requester,id).isPresent();
   if(!assigned)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Customer is not assigned to this user");
  }
  Customer c=customers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found"));
  if(role==Role.TELLER)return Map.of("id",c.getId(),"name",c.getName(),"active",c.isActive());
  return Map.of("id",c.getId(),"name",c.getName(),"email",c.getEmail().getValue(),"identification",c.getIdentification(),"active",c.isActive());
 }
}