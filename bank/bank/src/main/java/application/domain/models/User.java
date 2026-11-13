package application.domain.models;
import application.domain.enums.Role;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.time.LocalDate;
import java.util.Objects;
public final class User {
 private final String id,relatedEntityId,name,identification,address;
 private final Email email; private final PhoneNumber phone; private final LocalDate birthDate; private final Role role; private boolean active;
 public User(String id,String relatedEntityId,String name,String identification,Email email,PhoneNumber phone,LocalDate birthDate,String address,Role role,boolean active){
  if(id==null||id.isBlank()||relatedEntityId==null||relatedEntityId.isBlank())throw new IllegalArgumentException("User and related entity ids are required");
  this.id=id;this.relatedEntityId=relatedEntityId;this.name=Objects.requireNonNull(name);this.identification=Objects.requireNonNull(identification);
  this.email=Objects.requireNonNull(email);this.phone=phone;this.birthDate=birthDate;this.address=address==null?"":address;this.role=Objects.requireNonNull(role);this.active=active;
 }
 public String getId(){return id;} public String getRelatedEntityId(){return relatedEntityId;} public String getName(){return name;}
 public String getIdentification(){return identification;} public Email getEmail(){return email;} public PhoneNumber getPhone(){return phone;}
 public LocalDate getBirthDate(){return birthDate;} public String getAddress(){return address;} public Role getRole(){return role;}
 public boolean isActive(){return active;} public void deactivate(){active=false;}
}