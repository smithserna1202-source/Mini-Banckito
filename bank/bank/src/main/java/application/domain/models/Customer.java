package application.domain.models;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.time.LocalDate;
import java.util.Objects;
/** Customer profile for natural persons; companies have a separate model. */
public class Customer {
 private final String id,name,identification,address;
 private Email email; private PhoneNumber phone;
 private final LocalDate birthDate; private boolean active;
 public Customer(String id,String name,Email email){this(id,name,id,email,null,"",null,true);}
 public Customer(String id,String name,String identification,Email email,PhoneNumber phone,String address,LocalDate birthDate,boolean active){
  if(id==null||id.isBlank()||name==null||name.isBlank()||identification==null||identification.isBlank())throw new IllegalArgumentException("Customer identity fields are required");
  this.id=id;this.name=name;this.identification=identification;this.email=Objects.requireNonNull(email);this.phone=phone;this.address=address==null?"":address;this.birthDate=birthDate;this.active=active;
  if(birthDate!=null&&birthDate.isAfter(LocalDate.now().minusYears(18)))throw new IllegalArgumentException("Natural-person customers must be at least 18 years old");
 }
 public String getId(){return id;} public String getName(){return name;} public String getIdentification(){return identification;}
 public Email getEmail(){return email;} public PhoneNumber getPhone(){return phone;} public String getAddress(){return address;}
 public LocalDate getBirthDate(){return birthDate;} public boolean isActive(){return active;}
 public void setEmail(Email email){this.email=Objects.requireNonNull(email);} public void setPhone(PhoneNumber phone){this.phone=Objects.requireNonNull(phone);}
 public void deactivate(){active=false;}
}