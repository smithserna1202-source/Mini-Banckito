package application.adapters.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCommercialCustomerAssignmentRepository
        extends JpaRepository<CommercialCustomerAssignmentEntity, Long> {
    Optional<CommercialCustomerAssignmentEntity> findByEmployeeUsernameAndCustomerIdAndActiveTrue(
        String employeeUsername, String customerId);
}
