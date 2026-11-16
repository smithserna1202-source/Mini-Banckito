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
    public static String companyId(Authentication authentication) {
        Role role = role(authentication);
        if (role == Role.ADMINISTRATOR) return null;
        if (role != Role.COMPANY_CUSTOMER && role != Role.COMPANY_OPERATIONS_EMPLOYEE
                && role != Role.COMPANY_SUPERVISOR) {
            throw new SecurityException("User is not associated with a company");
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_COMPANY_SCOPE_"))
            .map(authority -> authority.substring("ROLE_COMPANY_SCOPE_".length()))
            .findFirst()
            .filter(value -> !value.isBlank())
            .orElseThrow(() -> new SecurityException("Authenticated user has no company assignment"));
    }

    public static Role role(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new SecurityException("Authentication required");
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5))
            .map(Role::valueOf).findFirst().orElseThrow(() -> new SecurityException("No banking role assigned"));
    }
}
