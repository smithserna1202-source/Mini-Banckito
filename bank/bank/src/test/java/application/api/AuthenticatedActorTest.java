package application.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthenticatedActorTest {
    @Test
    void resolvesCompanyFromAuthenticatedAuthorityRatherThanHardcodedId() {
        var authentication = new UsernamePasswordAuthenticationToken("operator-77", "n/a", List.of(
            new SimpleGrantedAuthority("ROLE_COMPANY_OPERATIONS_EMPLOYEE"),
            new SimpleGrantedAuthority("ROLE_COMPANY_SCOPE_COMPANY077")
        ));

        assertEquals("COMPANY077", AuthenticatedActor.companyId(authentication));
    }

    @Test
    void rejectsBusinessUserWithoutCompanyAssignment() {
        var authentication = new UsernamePasswordAuthenticationToken("operator-77", "n/a", List.of(
            new SimpleGrantedAuthority("ROLE_COMPANY_SUPERVISOR")
        ));

        assertThrows(SecurityException.class, () -> AuthenticatedActor.companyId(authentication));
    }

    @Test
    void administratorDoesNotHaveAnImplicitCompanyScope() {
        var authentication = new UsernamePasswordAuthenticationToken("admin", "n/a", List.of(
            new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")
        ));

        assertNull(AuthenticatedActor.companyId(authentication));
    }
}
