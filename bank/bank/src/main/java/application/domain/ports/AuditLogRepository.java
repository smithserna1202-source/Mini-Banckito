package application.domain.ports;
import application.domain.models.AuditLogEntry;
import java.util.List;
public interface AuditLogRepository { void append(AuditLogEntry entry); List<AuditLogEntry> findAll(); }