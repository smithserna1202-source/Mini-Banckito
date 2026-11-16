package application.adapters.jpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "bank_users")
public class BankUserEntity implements UserDetails {
    @Id @Column(length = 100, nullable = false)
    private String username;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 40)
    private String role;
    @Column(nullable = false, length = 100)
    private String relatedEntityId;
    @Column(length = 100)
    private String companyId;
    @Column(nullable = false)
    private boolean enabled;
    @Column(length = 160)
    private String fullName;
    @Column(unique = true, length = 80)
    private String identification;
    @Column(unique = true, length = 180)
    private String email;
    @Column(length = 20)
    private String phone;
    private LocalDate birthDate;
    @Column(length = 300)
    private String address;

    protected BankUserEntity() {}

    /** Compatibility constructor for seeded/demo and lower-level tests. */
    public BankUserEntity(String username, String passwordHash, String role,
                          String relatedEntityId, String companyId, boolean enabled) {
        this(username, passwordHash, role, relatedEntityId, companyId, enabled,
            username, "DEMO-" + username, username + "@demo.local",
            "3000000000", LocalDate.of(1990, 1, 1), "Datos de demostración");
    }

    public BankUserEntity(String username, String passwordHash, String role,
                          String relatedEntityId, String companyId, boolean enabled,
                          String fullName, String identification, String email,
                          String phone, LocalDate birthDate, String address) {
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()
                || role == null || role.isBlank() || relatedEntityId == null || relatedEntityId.isBlank()
                || fullName == null || fullName.isBlank() || identification == null || identification.isBlank()
                || email == null || email.isBlank() || !email.contains("@")
                || phone == null || !phone.matches("\\d{7,15}")
                || birthDate == null || address == null || address.isBlank()) {
            throw new IllegalArgumentException("All bank user identity and contact fields are required");
        }
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.relatedEntityId = relatedEntityId;
        this.companyId = companyId;
        this.enabled = enabled;
        this.fullName = fullName;
        this.identification = identification;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
    }

    public void deactivate() { this.enabled = false; }
    public void synchronizeDemoNaturalCustomerProfile() {
        fullName = "natural-customer";
        identification = "DEMO-C001";
        email = "natural-customer@demo.local";
        phone = "3000000000";
        birthDate = LocalDate.of(1990, 1, 1);
        address = "Datos de demostración";
    }
    public void completeMissingDemoProfile() {
        if (fullName == null || fullName.isBlank()) fullName = username;
        if (identification == null || identification.isBlank()) identification = "DEMO-" + username;
        if (email == null || email.isBlank()) email = username + "@demo.local";
        if (phone == null || phone.isBlank()) phone = "3000000000";
        if (birthDate == null) birthDate = LocalDate.of(1990, 1, 1);
        if (address == null || address.isBlank()) address = "Datos de demostración";
    }
    public String getRole() { return role; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public String getCompanyId() { return companyId; }
    public String getFullName() { return fullName; }
    public String getIdentification() { return identification; }
    public String getEmailAddress() { return email; }
    public String getPhoneNumber() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getAddress() { return address; }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return passwordHash; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        if (companyId != null && !companyId.isBlank())
            authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_SCOPE_" + companyId));
        return List.copyOf(authorities);
    }
}
