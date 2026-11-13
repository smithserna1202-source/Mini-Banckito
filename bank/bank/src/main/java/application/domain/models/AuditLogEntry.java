package application.domain.models;
import java.time.Instant;
import java.util.*;
/** Immutable audit event with a recursively immutable snapshot of its details. */
public final class AuditLogEntry {
 private final String id,operationType,userId,role,affectedProductId;
 private final Instant occurredAt;
 private final Map<String,Object> details;
 public AuditLogEntry(String id,String operationType,String userId,String role,String affectedProductId,Map<String,Object> details){
  this(id,operationType,Instant.now(),userId,role,affectedProductId,details);
 }
 public AuditLogEntry(String id,String operationType,Instant occurredAt,String userId,String role,String affectedProductId,Map<String,Object> details){
  this.id=Objects.requireNonNull(id);this.operationType=Objects.requireNonNull(operationType);this.occurredAt=Objects.requireNonNull(occurredAt);
  this.userId=Objects.requireNonNull(userId);this.role=Objects.requireNonNull(role);this.affectedProductId=affectedProductId;
  this.details=immutableMap(details==null?Map.of():details);
 }
 private static Map<String,Object> immutableMap(Map<String,Object> source){
  Map<String,Object> copy=new LinkedHashMap<>();
  source.forEach((key,value)->copy.put(Objects.requireNonNull(key),immutableValue(value)));
  return Collections.unmodifiableMap(copy);
 }
 private static Object immutableValue(Object value){
  if(value instanceof Map<?,?> map){
   Map<String,Object> nested=new LinkedHashMap<>();
   map.forEach((k,v)->nested.put(String.valueOf(k),immutableValue(v)));
   return Collections.unmodifiableMap(nested);
  }
  if(value instanceof Collection<?> collection)return collection.stream().map(AuditLogEntry::immutableValue).toList();
  if(value instanceof Object[] array)return Arrays.stream(array).map(AuditLogEntry::immutableValue).toList();
  return value;
 }
 public String getId(){return id;} public String getOperationType(){return operationType;} public Instant getOccurredAt(){return occurredAt;}
 public String getUserId(){return userId;} public String getRole(){return role;} public String getAffectedProductId(){return affectedProductId;}
 public Map<String,Object> getDetails(){return details;}
}
