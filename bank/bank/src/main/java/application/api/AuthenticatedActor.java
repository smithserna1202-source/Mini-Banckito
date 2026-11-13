package application.api;

import application.domain.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class AuthenticatedActor {
    private AuthenticatedActor() {}
    public static String id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new SecurityException("Authentication required");
        return authentication.getName();
    }
    public static Role role(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new SecurityException("Authentication required");
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5))
            .map(Role::valueOf).findFirst().orElseThrow(() -> new SecurityException("No banking role assigned"));
    }
}
