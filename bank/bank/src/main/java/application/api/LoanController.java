package application.api;

import application.adapters.jpa.SpringDataCommercialCustomerAssignmentRepository;
import application.domain.enums.LoanStatus;
import application.domain.enums.Role;
import application.domain.models.Loan;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.LoanRepository;
import application.domain.services.LoanService;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loans;
    private final LoanRepository repository;
    private final SpringDataCommercialCustomerAssignmentRepository assignments;

    public LoanController(LoanService loans, LoanRepository repository,
                          SpringDataCommercialCustomerAssignmentRepository assignments,
                          BankAccountRepository accounts) {
        this.loans = loans;
        this.repository = repository;
        this.assignments = assignments;
    }

    public record RequestLoan(@NotBlank String id, @NotBlank String type,
        @NotNull @DecimalMin("0.01") BigDecimal amount, @Min(1) int termMonths,
        @NotBlank String destinationAccount, String applicantId) {}
    public record Decision(@NotNull @DecimalMin("0.01") BigDecimal approvedAmount,
        @NotNull @DecimalMin("0.00") BigDecimal interestRate) {}

    @PostMapping
    public Map<String, String> request(@Valid @RequestBody RequestLoan request, Authentication actor) {
        Role role = AuthenticatedActor.role(actor);
        String applicant;
        if (role == Role.NATURAL_PERSON_CUSTOMER) {
            applicant = AuthenticatedActor.id(actor);
        } else if (role == Role.COMPANY_CUSTOMER) {
            applicant = AuthenticatedActor.companyId(actor);
        } else if (role == Role.COMMERCIAL_EMPLOYEE) {
            applicant = request.applicantId();
            if (applicant == null || assignments.findByEmployeeUsernameAndCustomerIdAndActiveTrue(
                    AuthenticatedActor.id(actor), applicant).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Commercial employee may request loans only for assigned customers");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role cannot request loans");
        }

        Loan loan = new Loan(request.id(), applicant, request.type(),
            Money.of(request.amount().toPlainString()), request.termMonths(), request.destinationAccount());
        loans.request(loan);
        return Map.of("loanId", loan.getId(), "status", loan.getStatus().name());
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id, Authentication actor) {
        Loan loan = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        Role role = AuthenticatedActor.role(actor);
        String requester = AuthenticatedActor.id(actor);
        boolean allowed = role == Role.ADMINISTRATOR || role == Role.INTERNAL_ANALYST;

        if (role == Role.NATURAL_PERSON_CUSTOMER) {
            allowed = loan.getApplicantId().equals(requester);
        } else if (role == Role.COMPANY_CUSTOMER || role == Role.COMPANY_OPERATIONS_EMPLOYEE
                || role == Role.COMPANY_SUPERVISOR) {
            allowed = loan.getApplicantId().equals(AuthenticatedActor.companyId(actor));
        } else if (role == Role.COMMERCIAL_EMPLOYEE) {
            allowed = assignments.findByEmployeeUsernameAndCustomerIdAndActiveTrue(
                    requester, loan.getApplicantId()).isPresent()
                && (loan.getStatus() == LoanStatus.UNDER_REVIEW || loan.getStatus() == LoanStatus.REJECTED);
        }

        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Loan is not available to this user");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loanId", loan.getId());
        response.put("status", loan.getStatus().name());
        response.put("requestedAt", loan.getRequestedAt().toString());
        response.put("requestedAmount", loan.getRequestedAmount().toString());
        if (role != Role.COMMERCIAL_EMPLOYEE) {
            response.put("applicantId", loan.getApplicantId());
            response.put("type", loan.getLoanType());
            response.put("termMonths", loan.getTermMonths());
            response.put("approvedAmount", loan.getApprovedAmount() == null ? null : loan.getApprovedAmount().toString());
            response.put("interestRate", loan.getInterestRate());
            response.put("approvedAt", loan.getApprovedAt());
            response.put("disbursedAt", loan.getDisbursedAt());
            response.put("destinationAccount", loan.getDestinationAccount());
        }
        return response;
    }

    @PostMapping("/{id}/approve")
    public Map<String, String> approve(@PathVariable String id, @Valid @RequestBody Decision decision,
                                       Authentication actor) {
        Role role = AuthenticatedActor.role(actor);
        loans.approve(id, AuthenticatedActor.id(actor), role,
            Money.of(decision.approvedAmount().toPlainString()), decision.interestRate());
        return Map.of("loanId", id, "status", repository.findById(id).orElseThrow().getStatus().name());
    }

    @PostMapping("/{id}/reject")
    public Map<String, String> reject(@PathVariable String id, Authentication actor) {
        Role role = AuthenticatedActor.role(actor);
        loans.reject(id, AuthenticatedActor.id(actor), role);
        return Map.of("loanId", id, "status", "REJECTED");
    }

    @PostMapping("/{id}/disburse")
    public Map<String, String> disburse(@PathVariable String id, Authentication actor) {
        if (AuthenticatedActor.role(actor) != Role.INTERNAL_ANALYST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only an analyst can disburse a loan");
        }
        loans.disburse(id, AuthenticatedActor.id(actor), Role.INTERNAL_ANALYST);
        return Map.of("loanId", id, "status", "DISBURSED");
    }
}
