package application.adapters.jpa;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCommercialCustomerAssignmentRepository
        extends JpaRepository<CommercialCustomerAssignmentEntity, Long> {
    Optional<CommercialCustomerAssignmentEntity> findByEmployeeUsernameAndCustomerIdAndActiveTrue(
        String employeeUsername, String customerId);
    List<CommercialCustomerAssignmentEntity> findByEmployeeUsernameAndActiveTrue(String employeeUsername);
}
