package application.api;

import application.domain.enums.Role;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.ports.AuditLogRepository;
import application.domain.ports.BankAccountRepository;
import application.domain.ports.LoanRepository;
import application.domain.ports.BankTransferRepository;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {
    private final AuditLogRepository audit;
    private final BankAccountRepository accounts;
    private final LoanRepository loans;
    private final BankTransferRepository transfers;

    public AuditLogController(AuditLogRepository audit, BankAccountRepository accounts, LoanRepository loans, BankTransferRepository transfers) {
        this.audit = audit;
        this.accounts = accounts;
        this.loans = loans;
        this.transfers = transfers;
    }

    @GetMapping
    public Object list(Authentication actor) {
        Role role = AuthenticatedActor.role(actor);
        if (role != Role.INTERNAL_ANALYST && role != Role.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Audit access denied");
        }
        return audit.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/product/{productId}")
    public Object productHistory(@PathVariable String productId, Authentication actor) {
        Role role = AuthenticatedActor.role(actor);
        if (role != Role.NATURAL_PERSON_CUSTOMER && role != Role.COMPANY_CUSTOMER
                && role != Role.COMPANY_OPERATIONS_EMPLOYEE && role != Role.COMPANY_SUPERVISOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer product history only");
        }
        String requester = AuthenticatedActor.id(actor);
        String ownerScope = role == Role.NATURAL_PERSON_CUSTOMER ? requester : AuthenticatedActor.companyId(actor);
        boolean ownsProduct = accounts.findByAccountNumber(productId)
            .map(account -> account.getOwnerId().equals(ownerScope))
            .orElseGet(() -> loans.findById(productId)
                .map(loan -> loan.getApplicantId().equals(ownerScope))
                .orElseGet(() -> transfers.findById(productId)
                    .map(transfer -> {
                        boolean ownsSource = accounts.findByAccountNumber(transfer.getSourceAccount())
                            .map(account -> account.getOwnerId().equals(ownerScope)).orElse(false);
                        boolean ownsDestination = accounts.findByAccountNumber(transfer.getDestinationAccount())
                            .map(account -> account.getOwnerId().equals(requester)).orElse(false);
                        return ownsSource || ownsDestination;
                    }).orElse(false)));
        if (!ownsProduct) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return audit.findAll().stream()
            .filter(entry -> productId.equals(entry.getAffectedProductId()))
            .map(this::toResponse)
            .toList();
    }

    private Map<String, Object> toResponse(application.domain.models.AuditLogEntry entry) {
        return Map.of(
            "id", entry.getId(),
            "operation", entry.getOperationType(),
            "occurredAt", entry.getOccurredAt().toString(),
            "userId", entry.getUserId(),
            "role", entry.getRole(),
            "productId", entry.getAffectedProductId() == null ? "" : entry.getAffectedProductId(),
            "details", entry.getDetails()
        );
    }
}
