package application.adapters.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataScheduledTransferRepository extends JpaRepository<ScheduledTransferEntity, String> {}
