package application.adapters;

import application.domain.models.BankTransfer;
import application.domain.ports.BankTransferRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryBankTransferRepository implements BankTransferRepository {
    private final ConcurrentMap<String, BankTransfer> transfers = new ConcurrentHashMap<>();
    @Override public void save(BankTransfer transfer) { transfers.put(transfer.getId(), transfer); }
    @Override public Optional<BankTransfer> findById(String id) { return Optional.ofNullable(transfers.get(id)); }
    @Override public List<BankTransfer> findAll() { return List.copyOf(transfers.values()); }
}
