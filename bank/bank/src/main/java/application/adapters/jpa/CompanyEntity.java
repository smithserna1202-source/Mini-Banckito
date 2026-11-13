package application.adapters.jpa;
import application.domain.models.Company;
import jakarta.persistence.*;
@Entity @Table(name="companies",uniqueConstraints=@UniqueConstraint(columnNames="nit"))
public class CompanyEntity {
 @Id private String id;
 @Column(nullable=false) private String legalName;
 @Column(nullable=false,unique=true) private String nit;
 @Column(nullable=false,unique=true) private String email;
 private String phone;
 private String address;
 @Column(nullable=false) private String legalRepresentativeId;
 @Column(nullable=false) private boolean active;
 protected CompanyEntity(){}
 public CompanyEntity(Company c){id=c.getId();legalName=c.getLegalName();nit=c.getNit();email=c.getEmail().getValue();phone=c.getPhone()==null?null:c.getPhone().getNumber();address=c.getAddress();legalRepresentativeId=c.getLegalRepresentativeId();active=c.isActive();}
 public String getId(){return id;} public String getLegalName(){return legalName;} public String getNit(){return nit;} public String getEmail(){return email;}
 public String getPhone(){return phone;} public String getAddress(){return address;} public String getLegalRepresentativeId(){return legalRepresentativeId;} public boolean isActive(){return active;}
}
