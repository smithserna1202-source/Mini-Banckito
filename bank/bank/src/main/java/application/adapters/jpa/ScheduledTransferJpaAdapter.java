package application.adapters.jpa;

import application.domain.models.ScheduledTransfer;
import application.domain.ports.ScheduledTransferRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("sql")
public class ScheduledTransferJpaAdapter implements ScheduledTransferRepository {
    private final SpringDataScheduledTransferRepository repository;

    public ScheduledTransferJpaAdapter(SpringDataScheduledTransferRepository repository) {
        this.repository = repository;
    }

    @Override public void save(ScheduledTransfer transfer) {
        repository.save(new ScheduledTransferEntity(transfer));
    }

    @Override public Optional<ScheduledTransfer> findById(String id) {
        return repository.findById(id).map(ScheduledTransferEntity::toDomain);
    }

    @Override public List<ScheduledTransfer> findAll() {
        return repository.findAll().stream().map(ScheduledTransferEntity::toDomain).toList();
    }
}
