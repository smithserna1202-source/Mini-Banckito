package application.adapters.jpa;

import application.domain.enums.Role;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

/**
 * Creates non-production demonstration users on first startup only.
 * Existing users are never overwritten, so password and enabled-state changes persist.
 */
@Component
@Profile("sql")
public class BankUserBootstrap implements ApplicationRunner {
    private static final String[][] USERS = {
        {"natural-customer", "NATURAL_PERSON_CUSTOMER", "C001", ""},
        {"company-customer", "COMPANY_CUSTOMER", "COMPANY001", "COMPANY001"},
        {"teller", "TELLER", "teller01", ""},
        {"commercial", "COMMERCIAL_EMPLOYEE", "commercial01", ""},
        {"company-operator", "COMPANY_OPERATIONS_EMPLOYEE", "company-operator", "COMPANY001"},
        {"company-supervisor", "COMPANY_SUPERVISOR", "supervisor01", "COMPANY001"},
        {"analyst", "INTERNAL_ANALYST", "analyst01", ""},
        {"administrator", "ADMINISTRATOR", "admin01", ""}
    };

    private final SpringDataBankUserRepository users;
    private final PasswordEncoder encoder;
    private final Environment environment;

    public BankUserBootstrap(SpringDataBankUserRepository users, PasswordEncoder encoder,
                             Environment environment) {
        this.users = users;
        this.encoder = encoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String sharedPassword = environment.getProperty("BANK_DEFAULT_PASSWORD");
        if (sharedPassword == null || sharedPassword.length() < 12) {
            throw new IllegalStateException("Set BANK_DEFAULT_PASSWORD to at least 12 characters");
        }

        for (String[] definition : USERS) {
            String username = environment.getProperty("bank.security.users." + definition[0], definition[2]);
            if (users.existsById(username)) {
                BankUserEntity existing = users.findById(username).orElseThrow();
                existing.completeMissingDemoProfile();
                users.save(existing);
                continue;
            }

            String passwordKey = "BANK_PASSWORD_" + definition[0].replace('-', '_').toUpperCase();
            String rawPassword = environment.getProperty(passwordKey, sharedPassword);
            String companyId = definition[3].isBlank() ? null
                : environment.getProperty("BANK_COMPANY_ID_" + definition[0].replace('-', '_').toUpperCase(),
                    definition[3]);
            String relatedEntityId = companyId == null ? definition[2] : companyId;

            users.save(new BankUserEntity(username, encoder.encode(rawPassword),
                Role.valueOf(definition[1]).name(), relatedEntityId, companyId, true));
        }
    }
}
