package application.adapters;
import application.domain.models.BankProduct;
import application.domain.ports.BankProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class InMemoryBankProductRepository implements BankProductRepository {
 private final ConcurrentMap<String,BankProduct> products=new ConcurrentHashMap<>();
 @Override public void save(BankProduct p){products.put(p.getId(),p);}
 @Override public Optional<BankProduct> findById(String id){return Optional.ofNullable(products.get(id));}
 @Override public List<BankProduct> findAllActive(){return products.values().stream().filter(BankProduct::isActive).toList();}
}