package application.domain.services;
import application.domain.enums.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuthorizationServiceTest {
 @Test void tellerCannotApproveLoans(){
  AuthorizationService auth=new AuthorizationService();
  assertFalse(auth.isAllowed(Role.TELLER,AuthorizationService.Action.APPROVE_LOAN));
  assertThrows(SecurityException.class,()->auth.require(Role.TELLER,AuthorizationService.Action.APPROVE_LOAN));
 }
 @Test void analystCanApproveLoansButCannotPerformCashOperations(){
  AuthorizationService auth=new AuthorizationService();
  assertTrue(auth.isAllowed(Role.INTERNAL_ANALYST,AuthorizationService.Action.APPROVE_LOAN));
  assertFalse(auth.isAllowed(Role.INTERNAL_ANALYST,AuthorizationService.Action.WITHDRAW));
 }
}