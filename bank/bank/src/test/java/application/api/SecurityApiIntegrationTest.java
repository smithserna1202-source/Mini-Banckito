package application.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import application.domain.models.AuditLogEntry;
import application.domain.ports.AuditLogRepository;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.CustomerRepository;
import application.domain.models.BankAccount;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import application.domain.enums.AccountType;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import application.adapters.jpa.BankUserEntity;
import application.adapters.jpa.SpringDataBankUserRepository;
import application.adapters.jpa.JpaBankUserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "BANK_DEFAULT_PASSWORD=Integration-Only-Password-123",
    "spring.datasource.url=jdbc:h2:mem:security-test;DB_CLOSE_DELAY=-1"
})
class SecurityApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private BankAccountRepository accounts;
    @Autowired private AuditLogRepository audit;
    @Autowired private CustomerRepository customers;
    @Autowired private application.domain.ports.CompanyRepository companies;
    @Autowired private application.domain.ports.BankProductRepository products;
    @Autowired private SpringDataBankUserRepository bankUsers;
    @Autowired private JpaBankUserDetailsService bankUserDetails;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void accountEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/accounts/0000000001/balance")).andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotOpenAccount() throws Exception {
        String body = """
            {"accountNumber":"1234567890","ownerId":"C001","type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("C001", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isForbidden());
    }
    @Test
    void customerCanOnlyReadAuditEventsForTheirOwnAccount() throws Exception {
        String own = "6000000001";
        String other = "6000000002";
        accounts.save(new BankAccount(new AccountNumber(own), Money.zero(), "C001", AccountType.SAVINGS, "COP"));
        accounts.save(new BankAccount(new AccountNumber(other), Money.zero(), "C002", AccountType.SAVINGS, "COP"));
        audit.append(new AuditLogEntry("audit-own-1", "ACCOUNT_OPENED", "C001",
            "NATURAL_PERSON_CUSTOMER", own, Map.of("source", "test")));
        audit.append(new AuditLogEntry("audit-other-1", "ACCOUNT_OPENED", "C002",
            "NATURAL_PERSON_CUSTOMER", other, Map.of("source", "test")));

        mvc.perform(get("/api/audit/product/" + own)
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("C001", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(get("/api/audit/product/" + other)
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("C001", "Integration-Only-Password-123")))
            .andExpect(status().isNotFound());
    }

    @Test
    void companyOperatorCanOnlyViewAccountsInTheirCompany() throws Exception {
        accounts.save(new BankAccount(new AccountNumber("6000000003"), Money.zero(),
            "COMPANY001", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("6000000004"), Money.zero(),
            "COMPANY002", AccountType.CHECKING, "COP"));

        mvc.perform(get("/api/accounts/6000000003/balance")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("company-operator", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(get("/api/accounts/6000000004/balance")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("company-operator", "Integration-Only-Password-123")))
            .andExpect(status().isForbidden());
    }

    @Test
    void credentialsAndCompanyRoleAreLoadedFromPersistentUserRepository() {
        bankUsers.save(new BankUserEntity("persisted-operator",
            passwordEncoder.encode("Persisted-Password-123"), "COMPANY_OPERATIONS_EMPLOYEE",
            "COMPANY077", "COMPANY077", true));

        var loaded = bankUserDetails.loadUserByUsername("persisted-operator");
        assertTrue(loaded.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY_OPERATIONS_EMPLOYEE")));
        assertTrue(loaded.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY_SCOPE_COMPANY077")));
    }

    @Test
    void disabledPersistentUserCannotAuthenticate() {
        bankUsers.save(new BankUserEntity("disabled-user",
            passwordEncoder.encode("Disabled-Password-123"), "TELLER", "teller-disabled", null, false));

        assertThrows(UsernameNotFoundException.class,
            () -> bankUserDetails.loadUserByUsername("disabled-user"));
    }

    @Test
    void onlyAdministratorCanCreateAndDeactivatePersistentUsers() throws Exception {
        String body = """
            {"username":"api-managed-teller","password":"New-Strong-Password-123",
             "role":"TELLER","relatedEntityId":"TELLER-TEST","companyId":null,
             "fullName":"Demo Teller","identification":"ID-TELLER-77","email":"teller77@example.test",
             "phone":"3001234577","birthDate":"1990-01-01","address":"Test address"}
            """;

        mvc.perform(post("/api/admin/users")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/admin/users")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("C001", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/admin/users/api-managed-teller/deactivate")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        assertThrows(UsernameNotFoundException.class,
            () -> bankUserDetails.loadUserByUsername("api-managed-teller"));
    }

    @Test
    void commercialEmployeeCanOnlyReadCustomersAssignedByAdministrator() throws Exception {
        String customerId = "CUSTOMER-ASSIGNED-77";
        customers.save(new application.domain.models.Customer(customerId, "Assigned Customer",
            "ID-ASSIGNED-77", new application.domain.valueobjects.Email("assigned77@example.test"),
            new application.domain.valueobjects.PhoneNumber("3001234567"), "Test address",
            java.time.LocalDate.of(1990, 1, 1), true));

        String employeeBody = """
            {"username":"commercial-assigned-77","password":"Commercial-Password-123",
             "role":"COMMERCIAL_EMPLOYEE","relatedEntityId":"STAFF-77","companyId":null,
             "fullName":"Commercial Employee","identification":"ID-COMM-77","email":"commercial77@example.test",
             "phone":"3001234578","birthDate":"1990-01-01","address":"Test address"}
            """;
        mvc.perform(post("/api/admin/users")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(employeeBody))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/admin/users/commercial-assigned-77/customers/" + customerId)
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/customers/" + customerId)
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123")))
            .andExpect(status().isOk());

        String accountRequest = """
            {"accountNumber":"7000000077","ownerId":"CUSTOMER-ASSIGNED-77",
             "type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(accountRequest))
            .andExpect(status().isOk());

        String fundedAccountRequest = """
            {"accountNumber":"7000000078","ownerId":"CUSTOMER-ASSIGNED-77",
             "type":"SAVINGS","initialBalance":100,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(fundedAccountRequest))
            .andExpect(status().isForbidden());

        String loanRequest = """
            {"id":"loan-assigned-77","type":"PERSONAL","amount":1000,
             "termMonths":12,"destinationAccount":"7000000077","applicantId":"CUSTOMER-ASSIGNED-77"}
            """;
        mvc.perform(post("/api/loans")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(loanRequest))
            .andExpect(status().isOk());

        mvc.perform(get("/api/loans/loan-assigned-77")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(get("/api/loans")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(get("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123")))
            .andExpect(status().isOk());

        String unassignedLoanRequest = """
            {"id":"loan-unassigned-77","type":"PERSONAL","amount":1000,
             "termMonths":12,"destinationAccount":"7000000077","applicantId":"C001"}
            """;
        mvc.perform(post("/api/loans")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(unassignedLoanRequest))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/customers/C001")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("commercial-assigned-77", "Commercial-Password-123")))
            .andExpect(status().isForbidden());
    }

    @Test
    void companyManagerCanManageOnlyOperationalUsersWithinItsCompany() throws Exception {
        companies.save(new application.domain.models.Company("COMPANY001", "Test Company",
            "900123456-7", new application.domain.valueobjects.Email("company001@example.test"),
            new application.domain.valueobjects.PhoneNumber("3001234580"), "Company address", "C001"));

        String body = """
            {"username":"company-operator-managed","password":"Company-User-Password-123",
             "role":"COMPANY_OPERATIONS_EMPLOYEE","relatedEntityId":"COMPANY001","companyId":"COMPANY001",
             "fullName":"Company Operator","identification":"ID-COMP-OP-77","email":"companyop77@example.test",
             "phone":"3001234581","birthDate":"1990-01-01","address":"Company address"}
            """;
        mvc.perform(post("/api/admin/users")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("COMPANY001", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/admin/users/company-operator-managed/deactivate")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("COMPANY001", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        assertThrows(UsernameNotFoundException.class,
            () -> bankUserDetails.loadUserByUsername("company-operator-managed"));
    }

    @Test
    void tellerCannotSeeProductInterestRateDetails() throws Exception {
        products.save(new application.domain.models.BankProduct("LOAN-RISK-TEST", "Risk Test Loan",
            application.domain.enums.ProductType.LOAN, "Test product", new java.math.BigDecimal("12.50")));

        String response = mvc.perform(get("/api/products")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertFalse(response.contains("interestRate"));
    }

    @Test
    void tellerCanRegisterNewCustomerAndOpenTheirAccount() throws Exception {
        String customerBody = """
            {"customerId":"TELLER-CUSTOMER-77","name":"New Bank Customer",
             "identification":"ID-TELLER-CUSTOMER-77","email":"newcustomer77@example.test",
             "phone":"3001234590","birthDate":"1990-01-01","address":"Customer address"}
            """;
        mvc.perform(post("/api/customers")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(customerBody))
            .andExpect(status().isOk());

        String accountBody = """
            {"accountNumber":"7000000079","ownerId":"TELLER-CUSTOMER-77",
             "type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(accountBody))
            .andExpect(status().isOk());

        mvc.perform(get("/api/customers/TELLER-CUSTOMER-77")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        String userBody = """
            {"username":"TELLER-CUSTOMER-77","password":"New-Customer-Password-123",
             "role":"NATURAL_PERSON_CUSTOMER","relatedEntityId":"TELLER-CUSTOMER-77","companyId":null,
             "fullName":"New Bank Customer","identification":"ID-TELLER-CUSTOMER-77",
             "email":"newcustomer77@example.test","phone":"3001234590",
             "birthDate":"1990-01-01","address":"Customer address"}
            """;
        mvc.perform(post("/api/admin/users")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(userBody))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/accounts/7000000079/balance")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("TELLER-CUSTOMER-77", "New-Customer-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(post("/api/admin/users/TELLER-CUSTOMER-77/deactivate")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        String blockedAccountBody = """
            {"accountNumber":"7000000091","ownerId":"TELLER-CUSTOMER-77",
             "type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(blockedAccountBody))
            .andExpect(status().isConflict());

        mvc.perform(post("/api/admin/users/TELLER-CUSTOMER-77/activate")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(blockedAccountBody))
            .andExpect(status().isOk());
    }

    @Test
    void companyOperatorCanSubmitBatchPaymentsFromItsOwnAccount() throws Exception {
        accounts.save(new BankAccount(new AccountNumber("8000000081"), Money.of("1000000"),
            "COMPANY001", AccountType.CHECKING, "COP"));
        accounts.save(new BankAccount(new AccountNumber("8000000082"), Money.zero(),
            "PAYEE001", AccountType.SAVINGS, "COP"));
        accounts.save(new BankAccount(new AccountNumber("8000000083"), Money.zero(),
            "PAYEE002", AccountType.SAVINGS, "COP"));

        String body = """
            {"sourceAccount":"8000000081","payments":[
              {"destinationAccount":"8000000082","amount":100},
              {"destinationAccount":"8000000083","amount":200}
            ]}
            """;
        mvc.perform(post("/api/company-transfers/batch")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("company-operator", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        assertEquals(Money.of("999700"),
            accounts.findByAccountNumber("8000000081").orElseThrow().getBalance());
        assertEquals(Money.of("100"), accounts.findByAccountNumber("8000000082").orElseThrow().getBalance());
        assertEquals(Money.of("200"), accounts.findByAccountNumber("8000000083").orElseThrow().getBalance());
    }

    @Test
    void seededCatalogMarksLoanProductsAsApprovalRequired() {
        assertTrue(products.findById("LOAN_PERSONAL").orElseThrow().isRequiresApproval());
        assertTrue(products.findById("CREDIT_CARD").orElseThrow().isRequiresApproval());
    }

    @Test
    void blockedCustomerCannotOpenAccountUntilAdministratorUnblocksThem() throws Exception {
        mvc.perform(post("/api/customers/C001/block")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        String accountBody = """
            {"accountNumber":"7000000090","ownerId":"C001",
             "type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(accountBody))
            .andExpect(status().isConflict());

        mvc.perform(post("/api/customers/C001/unblock")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin01", "Integration-Only-Password-123")))
            .andExpect(status().isOk());

        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller01", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON).content(accountBody))
            .andExpect(status().isOk());
    }

}