package application.api;
import application.domain.enums.ProductType;
import application.domain.models.BankProduct;
import application.domain.ports.BankProductRepository;
import application.domain.enums.Role;
import application.domain.services.AuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/products")
public class BankProductController {
 private final BankProductRepository products;private final AuditService audit;
 public BankProductController(BankProductRepository p,AuditService a){products=p;audit=a;}
 public record CreateProduct(@NotBlank String id,@NotBlank String name,@NotNull ProductType type,String description,@DecimalMin("0.00") BigDecimal interestRate){}
 @GetMapping public Object list(){return products.findAllActive().stream().map(p->Map.of("id",p.getId(),"name",p.getName(),"type",p.getType(),"description",p.getDescription(),"interestRate",p.getInterestRate()==null?"":p.getInterestRate().toPlainString())).toList();}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateProduct r,Authentication actor){
  if(AuthenticatedActor.role(actor)!=Role.ADMINISTRATOR)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator role required");
  if(products.findById(r.id()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Product id already exists");
  BankProduct p=new BankProduct(r.id(),r.name(),r.type(),r.description(),r.interestRate());products.save(p);
  audit.record("PRODUCT_CREATED",AuthenticatedActor.id(actor),Role.ADMINISTRATOR.name(),p.getId(),Map.of("type",p.getType().name()));
  return Map.of("id",p.getId(),"name",p.getName(),"type",p.getType(),"active",p.isActive());
 }
}