package application.api;
import application.domain.enums.Role;
import application.domain.models.Company;
import application.domain.ports.CompanyRepository;
import application.domain.services.AuditService;
import application.domain.valueobjects.PhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/companies")
public class CompanyController {
 private final CompanyRepository companies;private final AuditService audit;
 public CompanyController(CompanyRepository c,AuditService a){companies=c;audit=a;}
 public record CreateCompany(@NotBlank String id,@NotBlank String legalName,@NotBlank String nit,@jakarta.validation.constraints.Email String email,@Pattern(regexp="\\d{7,15}") String phone,String address,@NotBlank String legalRepresentativeId){}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateCompany r,Authentication actor){
  if(AuthenticatedActor.role(actor)!=Role.ADMINISTRATOR)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator role required");
  if(companies.findById(r.id()).isPresent()||companies.findByNit(r.nit()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Company id or NIT already exists");
  Company c=new Company(r.id(),r.legalName(),r.nit(),new Email(r.email()),r.phone()==null?null:new PhoneNumber(r.phone()),r.address(),r.legalRepresentativeId());
  companies.save(c);audit.record("COMPANY_REGISTERED",AuthenticatedActor.id(actor),Role.ADMINISTRATOR.name(),c.getId(),Map.of("nit",c.getNit()));
  return Map.of("id",c.getId(),"legalName",c.getLegalName(),"nit",c.getNit(),"active",c.isActive());
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable String id,Authentication actor){
  Role role=AuthenticatedActor.role(actor);if(role!=Role.ADMINISTRATOR&&!AuthenticatedActor.id(actor).equals(id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Company access denied");
  Company c=companies.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Company not found"));
  return Map.of("id",c.getId(),"legalName",c.getLegalName(),"nit",c.getNit(),"active",c.isActive());
 }
}