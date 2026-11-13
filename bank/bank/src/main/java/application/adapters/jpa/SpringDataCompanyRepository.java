package application.adapters.jpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SpringDataCompanyRepository extends JpaRepository<CompanyEntity,String> {
 Optional<CompanyEntity> findByNit(String nit);
}
