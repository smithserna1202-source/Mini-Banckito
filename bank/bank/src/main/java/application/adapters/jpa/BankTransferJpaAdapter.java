package application.adapters.jpa;

import application.domain.models.BankTransfer;
import application.domain.ports.BankTransferRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("sql")
public class BankTransferJpaAdapter implements BankTransferRepository {
    private final SpringDataBankTransferRepository repository;

    public BankTransferJpaAdapter(SpringDataBankTransferRepository repository) {
        this.repository = repository;
    }

    @Override public void save(BankTransfer transfer) {
        repository.save(new BankTransferEntity(transfer));
    }

    @Override public Optional<BankTransfer> findById(String id) {
        return repository.findById(id).map(BankTransferEntity::toDomain);
    }

    @Override public List<BankTransfer> findAll() {
        return repository.findAll().stream().map(BankTransferEntity::toDomain).toList();
    }
}
