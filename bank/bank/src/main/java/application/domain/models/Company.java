package application.domain.models;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import java.util.Objects;
public final class Company {
 private final String id,legalName,nit,address,legalRepresentativeId;
 private final Email email; private final PhoneNumber phone; private boolean active=true;
 public Company(String id,String legalName,String nit,Email email,PhoneNumber phone,String address,String legalRepresentativeId){
  if(id==null||id.isBlank()||legalName==null||legalName.isBlank()||nit==null||nit.isBlank())throw new IllegalArgumentException("Company identity fields are required");
  this.id=id;this.legalName=legalName;this.nit=nit;this.email=Objects.requireNonNull(email);this.phone=phone;this.address=address==null?"":address;this.legalRepresentativeId=Objects.requireNonNull(legalRepresentativeId);
 }
 public String getId(){return id;} public String getLegalName(){return legalName;} public String getNit(){return nit;}
 public Email getEmail(){return email;} public PhoneNumber getPhone(){return phone;} public String getAddress(){return address;}
 public String getLegalRepresentativeId(){return legalRepresentativeId;} public boolean isActive(){return active;} public void deactivate(){active=false;}
}