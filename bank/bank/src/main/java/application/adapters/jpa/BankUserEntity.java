package application.adapters.jpa;

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

    protected BankUserEntity() {}

    public BankUserEntity(String username, String passwordHash, String role,
                          String relatedEntityId, String companyId, boolean enabled) {
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()
                || role == null || role.isBlank() || relatedEntityId == null || relatedEntityId.isBlank()) {
            throw new IllegalArgumentException("Username, password, role and related entity are required");
        }
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.relatedEntityId = relatedEntityId;
        this.companyId = companyId;
        this.enabled = enabled;
    }
    public String getRole() { return role; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public String getCompanyId() { return companyId; }
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
