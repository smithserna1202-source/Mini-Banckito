package application.api;

import application.adapters.jpa.BankUserEntity;
import application.adapters.jpa.CommercialCustomerAssignmentEntity;
import application.adapters.jpa.SpringDataCommercialCustomerAssignmentRepository;
import application.adapters.jpa.SpringDataBankUserRepository;
import application.domain.enums.Role;
import application.domain.ports.CompanyRepository;
import application.domain.ports.CustomerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
public class BankUserController {
    private final SpringDataBankUserRepository users;
    private final PasswordEncoder passwords;
    private final CustomerRepository customers;
    private final CompanyRepository companies;
    private final SpringDataCommercialCustomerAssignmentRepository assignments;

    public BankUserController(SpringDataBankUserRepository users, PasswordEncoder passwords,
                              CustomerRepository customers, CompanyRepository companies,
                              SpringDataCommercialCustomerAssignmentRepository assignments) {
        this.users = users;
        this.passwords = passwords;
        this.customers = customers;
        this.companies = companies;
        this.assignments = assignments;
    }

    public record CreateUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 100) String password,
        @NotNull Role role,
        @NotBlank @Size(max = 100) String relatedEntityId,
        @Size(max = 100) String companyId,
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 80) String identification,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Pattern(regexp = "\\d{7,15}") String phone,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Size(max = 300) String address
    ) {}

    public record UserResponse(String username, String role, String relatedEntityId,
                               String companyId, String fullName, String identification,
                               String email, String phone, LocalDate birthDate, String address,
                               boolean enabled) {
        static UserResponse from(BankUserEntity user) {
            return new UserResponse(user.getUsername(), user.getRole(),
                user.getRelatedEntityId(), user.getCompanyId(), user.getFullName(),
                user.getIdentification(), user.getEmailAddress(), user.getPhoneNumber(),
                user.getBirthDate(), user.getAddress(), user.isEnabled());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                               Authentication authentication) {
        Role managingRole = AuthenticatedActor.role(authentication);
        boolean bankAdministrator = managingRole == Role.ADMINISTRATOR;
        if (!bankAdministrator) {
            if (managingRole != Role.COMPANY_CUSTOMER && managingRole != Role.COMPANY_SUPERVISOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators or company managers can create users");
            }
            String managerCompany = AuthenticatedActor.companyId(authentication);
            if (request.role() != Role.COMPANY_OPERATIONS_EMPLOYEE
                    || !managerCompany.equals(request.companyId())
                    || !managerCompany.equals(request.relatedEntityId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Company managers may create operational users only within their own company");
            }
        }
        if (users.existsById(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (users.existsByIdentification(request.identification())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User identification already exists");
        }
        if (users.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User email already exists");
        }

        Role role = request.role();
        String companyId = request.companyId();
        if (role == Role.NATURAL_PERSON_CUSTOMER) {
            if (companyId != null && !companyId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Natural customers cannot have a company scope");
            }
            customers.findById(request.relatedEntityId()).filter(c -> c.isActive())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "An active customer must be associated with this user"));
        } else if (role == Role.COMPANY_CUSTOMER || role == Role.COMPANY_OPERATIONS_EMPLOYEE
                || role == Role.COMPANY_SUPERVISOR) {
            if (companyId == null || companyId.isBlank()
                    || !companyId.equals(request.relatedEntityId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Business users must be associated with their own company identifier");
            }
            companies.findById(companyId).filter(c -> c.isActive())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "An active company must be associated with this user"));
        } else if (companyId != null && !companyId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Only business roles may have a company scope");
        }

        if (role == Role.NATURAL_PERSON_CUSTOMER
                && request.birthDate().isAfter(java.time.LocalDate.now().minusYears(18))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Natural-person customers must be at least 18 years old");
        }
        BankUserEntity user = new BankUserEntity(request.username(),
            passwords.encode(request.password()), role.name(), request.relatedEntityId(),
            companyId, true, request.fullName(), request.identification(), request.email(),
            request.phone(), request.birthDate(), request.address());
        return UserResponse.from(users.save(user));
    }

    @PostMapping("/{username}/deactivate")
    public UserResponse deactivate(@PathVariable String username, Authentication authentication) {
        Role managingRole = AuthenticatedActor.role(authentication);
        BankUserEntity user = users.findById(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (managingRole != Role.ADMINISTRATOR) {
            boolean companyManager = managingRole == Role.COMPANY_CUSTOMER || managingRole == Role.COMPANY_SUPERVISOR;
            String companyId = companyManager ? AuthenticatedActor.companyId(authentication) : null;
            if (!companyManager || !Role.COMPANY_OPERATIONS_EMPLOYEE.name().equals(user.getRole())
                    || !companyId.equals(user.getCompanyId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Company managers may deactivate only operational users of their own company");
            }
        }
        user.deactivate();
        return UserResponse.from(users.save(user));
    }


    @PostMapping("/{username}/customers/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignCustomer(@PathVariable String username, @PathVariable String customerId,
                               Authentication authentication) {
        requireAdministrator(authentication);
        BankUserEntity employee = users.findById(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!Role.COMMERCIAL_EMPLOYEE.name().equals(employee.getRole()) || !employee.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Only active commercial employees can be assigned customers");
        }
        customers.findById(customerId).filter(c -> c.isActive())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active customer not found"));
        if (assignments.findByEmployeeUsernameAndCustomerIdAndActiveTrue(username, customerId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer is already assigned to this employee");
        }
        assignments.save(new CommercialCustomerAssignmentEntity(username, customerId));
    }

    private static void requireAdministrator(Authentication authentication) {
        if (AuthenticatedActor.role(authentication) != Role.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role required");
        }
    }
}
