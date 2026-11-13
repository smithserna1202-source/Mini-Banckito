package application.adapters.jpa;
import application.domain.models.Company;
import application.domain.ports.CompanyRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile("sql")
public class CompanyJpaAdapter implements CompanyRepository {
 private final SpringDataCompanyRepository repository;
 public CompanyJpaAdapter(SpringDataCompanyRepository repository){this.repository=repository;}
 @Override public void save(Company company){repository.save(new CompanyEntity(company));}
 @Override public Optional<Company> findById(String id){return repository.findById(id).map(this::toDomain);}
 @Override public Optional<Company> findByNit(String nit){return repository.findByNit(nit).map(this::toDomain);}
 private Company toDomain(CompanyEntity e){
  Company c=new Company(e.getId(),e.getLegalName(),e.getNit(),new Email(e.getEmail()),e.getPhone()==null?null:new PhoneNumber(e.getPhone()),e.getAddress(),e.getLegalRepresentativeId());
  if(!e.isActive())c.deactivate();
  return c;
 }
}
