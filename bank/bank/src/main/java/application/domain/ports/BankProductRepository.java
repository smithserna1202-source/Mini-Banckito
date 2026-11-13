package application.domain.ports;
import application.domain.models.BankProduct;
import java.util.List;
import java.util.Optional;
public interface BankProductRepository { void save(BankProduct product); Optional<BankProduct> findById(String id); List<BankProduct> findAllActive(); }