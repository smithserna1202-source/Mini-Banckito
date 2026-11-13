package application.adapters.jpa;
import application.domain.models.BankProduct;
import application.domain.ports.BankProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class BankProductJpaAdapter implements BankProductRepository {
 private final SpringDataBankProductRepository repository;
 public BankProductJpaAdapter(SpringDataBankProductRepository repository){this.repository=repository;}
 @Override public void save(BankProduct product){repository.save(new BankProductEntity(product));}
 @Override public Optional<BankProduct> findById(String id){return repository.findById(id).map(this::toDomain);}
 @Override public List<BankProduct> findAllActive(){return repository.findByActiveTrue().stream().map(this::toDomain).toList();}
 private BankProduct toDomain(BankProductEntity e){
  BankProduct p=new BankProduct(e.getId(),e.getName(),e.getType(),e.getDescription(),e.getInterestRate());
  if(!e.isActive())p.deactivate();
  return p;
 }
}
