package application.domain.services;
import application.domain.enums.Role;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
/** Central role-to-action policy; callers must still enforce ownership and company scope. */
public class AuthorizationService {
 public enum Action { VIEW_OWN_PRODUCTS, REQUEST_LOAN, REQUEST_TRANSFER, VIEW_ACCOUNT_STATUS, DEPOSIT, WITHDRAW, OPEN_ACCOUNT, MANAGE_ASSIGNED_CUSTOMERS, REQUEST_PRODUCT, COMPANY_PAYMENT, APPROVE_HIGH_VALUE_TRANSFER, APPROVE_LOAN, VIEW_AUDIT_LOG, CHANGE_BALANCE }
 private static final Map<Role,Set<Action>> PERMISSIONS=new EnumMap<>(Role.class);
 static {
  PERMISSIONS.put(Role.NATURAL_PERSON_CUSTOMER,EnumSet.of(Action.VIEW_OWN_PRODUCTS,Action.REQUEST_LOAN,Action.REQUEST_TRANSFER));
  PERMISSIONS.put(Role.COMPANY_CUSTOMER,EnumSet.of(Action.VIEW_OWN_PRODUCTS,Action.REQUEST_TRANSFER));
  PERMISSIONS.put(Role.TELLER,EnumSet.of(Action.VIEW_ACCOUNT_STATUS,Action.DEPOSIT,Action.WITHDRAW,Action.OPEN_ACCOUNT));
  PERMISSIONS.put(Role.COMMERCIAL_EMPLOYEE,EnumSet.of(Action.MANAGE_ASSIGNED_CUSTOMERS,Action.REQUEST_PRODUCT));
  PERMISSIONS.put(Role.COMPANY_OPERATIONS_EMPLOYEE,EnumSet.of(Action.COMPANY_PAYMENT,Action.REQUEST_TRANSFER));
  PERMISSIONS.put(Role.COMPANY_SUPERVISOR,EnumSet.of(Action.APPROVE_HIGH_VALUE_TRANSFER));
  PERMISSIONS.put(Role.INTERNAL_ANALYST,EnumSet.of(Action.APPROVE_LOAN,Action.VIEW_AUDIT_LOG));
  PERMISSIONS.put(Role.ADMINISTRATOR,EnumSet.allOf(Action.class));
 }
 public boolean isAllowed(Role role,Action action){return role!=null&&action!=null&&PERMISSIONS.getOrDefault(role,Set.of()).contains(action);}
 public void require(Role role,Action action){if(!isAllowed(role,action))throw new SecurityException("Role "+role+" cannot perform "+action);}
 public boolean canAccessCustomerData(String requesterCustomerId,String ownerCustomerId){return requesterCustomerId!=null&&requesterCustomerId.equals(ownerCustomerId);}
}