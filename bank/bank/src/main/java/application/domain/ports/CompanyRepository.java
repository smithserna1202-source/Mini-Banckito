package application.domain.ports;
import application.domain.models.Company;
import java.util.Optional;
public interface CompanyRepository { void save(Company company); Optional<Company> findById(String id); Optional<Company> findByNit(String nit); }