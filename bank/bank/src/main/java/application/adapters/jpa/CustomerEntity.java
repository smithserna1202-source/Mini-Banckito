package application.adapters.jpa;
import application.domain.models.Customer;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="customers",uniqueConstraints=@UniqueConstraint(columnNames="identification"))
public class CustomerEntity {
 @Id private String id;
 @Column(nullable=false) private String name;
 @Column(nullable=false,unique=true) private String identification;
 @Column(nullable=false,unique=true) private String email;
 private String phone; private String address; private LocalDate birthDate; private boolean active;
 protected CustomerEntity(){}
 public CustomerEntity(Customer c){id=c.getId();name=c.getName();identification=c.getIdentification();email=c.getEmail().getValue();phone=c.getPhone()==null?null:c.getPhone().getNumber();address=c.getAddress();birthDate=c.getBirthDate();active=c.isActive();}
 public String getId(){return id;} public String getName(){return name;} public String getIdentification(){return identification;} public String getEmail(){return email;}
 public String getPhone(){return phone;} public String getAddress(){return address;} public LocalDate getBirthDate(){return birthDate;} public boolean isActive(){return active;}
}