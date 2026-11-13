package application.domain.services;
import application.domain.models.ScheduledTransfer;
import application.domain.ports.ScheduledTransferRepository;
import java.util.Map;
import java.util.Objects;
public class CancelScheduledTransferService {
 private final ScheduledTransferRepository repository;private final AuditService audit;
 public CancelScheduledTransferService(ScheduledTransferRepository repository,AuditService audit){this.repository=Objects.requireNonNull(repository);this.audit=Objects.requireNonNull(audit);}
 public void cancel(String id,String requesterId,String role){
  ScheduledTransfer t=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Scheduled transfer not found"));
  if(!t.getCreatorId().equals(requesterId))throw new SecurityException("Only the creator may cancel this transfer");
  t.cancel();repository.save(t);audit.record("TRANSFER_CANCELLED",requesterId,role,id,Map.of());
 }
}