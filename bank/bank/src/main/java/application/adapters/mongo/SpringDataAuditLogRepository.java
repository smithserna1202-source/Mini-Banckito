package application.adapters.mongo;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface SpringDataAuditLogRepository extends MongoRepository<AuditLogDocument,String> {}