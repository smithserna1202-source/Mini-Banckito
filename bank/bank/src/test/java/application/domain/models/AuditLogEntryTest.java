package application.domain.models;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuditLogEntryTest {
 @Test void detailsAreCopiedAndCannotBeChanged(){
  Map<String,Object> data=new HashMap<>();data.put("amount","100.00");
  AuditLogEntry entry=new AuditLogEntry("1","DEPOSIT","u1","TELLER","a1",data);
  data.put("amount","0");
  assertEquals("100.00",entry.getDetails().get("amount"));
  assertThrows(UnsupportedOperationException.class,()->entry.getDetails().put("x","y"));
 }
}