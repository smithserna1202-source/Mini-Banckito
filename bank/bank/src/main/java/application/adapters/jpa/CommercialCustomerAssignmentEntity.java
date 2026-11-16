package application.adapters.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "commercial_customer_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_username", "customer_id"}))
public class CommercialCustomerAssignmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_username", nullable = false, length = 100)
    private String employeeUsername;
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;
    @Column(nullable = false)
    private boolean active = true;

    protected CommercialCustomerAssignmentEntity() {}
    public CommercialCustomerAssignmentEntity(String employeeUsername, String customerId) {
        if (employeeUsername == null || employeeUsername.isBlank()
                || customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Employee username and customer id are required");
        }
        this.employeeUsername = employeeUsername;
        this.customerId = customerId;
    }
    public String getEmployeeUsername() { return employeeUsername; }
    public String getCustomerId() { return customerId; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
}
