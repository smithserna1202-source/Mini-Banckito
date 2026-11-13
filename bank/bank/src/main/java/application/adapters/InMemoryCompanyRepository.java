package application.adapters;
import application.domain.models.Company;
import application.domain.ports.CompanyRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class InMemoryCompanyRepository implements CompanyRepository {
 private final ConcurrentMap<String,Company> companies=new ConcurrentHashMap<>();
 @Override public void save(Company c){companies.put(c.getId(),c);}
 @Override public Optional<Company> findById(String id){return Optional.ofNullable(companies.get(id));}
 @Override public Optional<Company> findByNit(String nit){return companies.values().stream().filter(c->c.getNit().equals(nit)).findFirst();}
}