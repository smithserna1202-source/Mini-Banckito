package application.adapters.jpa;

import application.domain.enums.ProductType;
import application.domain.models.BankProduct;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds the standard account types required by the academic bank exercise. */
@Component
@Profile("sql")
public class BankProductBootstrap implements ApplicationRunner {
    private static final List<BankProduct> DEFAULT_PRODUCTS = List.of(
        new BankProduct("ACCOUNT_SAVINGS", "Cuenta de ahorros", ProductType.SAVINGS_ACCOUNT,
            "Cuenta de depósito de ahorros", null),
        new BankProduct("ACCOUNT_CHECKING", "Cuenta corriente", ProductType.CHECKING_ACCOUNT,
            "Cuenta corriente transaccional", null),
        new BankProduct("ACCOUNT_PERSONAL", "Cuenta personal", ProductType.PERSONAL_ACCOUNT,
            "Cuenta personal", null),
        new BankProduct("ACCOUNT_BUSINESS", "Cuenta empresarial", ProductType.BUSINESS_ACCOUNT,
            "Cuenta para clientes empresa", null),
        new BankProduct("LOAN_PERSONAL", "Crédito personal", ProductType.LOAN,
            "Producto de préstamo personal", null),
        new BankProduct("TERM_DEPOSIT", "Depósito a término", ProductType.TERM_DEPOSIT,
            "Producto de ahorro a término", null),
        new BankProduct("CREDIT_CARD", "Tarjeta de crédito", ProductType.CREDIT_CARD,
            "Producto de crédito", null)
    );

    private final SpringDataBankProductRepository products;

    public BankProductBootstrap(SpringDataBankProductRepository products) {
        this.products = products;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (BankProduct product : DEFAULT_PRODUCTS) {
            if (!products.existsById(product.getId())) {
                products.save(new BankProductEntity(product));
            }
        }
    }
}
