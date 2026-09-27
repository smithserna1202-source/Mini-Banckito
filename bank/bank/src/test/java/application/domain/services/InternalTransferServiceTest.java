package application.domain.services;

import application.domain.models.BankAccount;
import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InternalTransferServiceTest {
    @Test
    void testTransfer() {
        BankAccount source = new BankAccount(new AccountNumber("1111111111"), new Money(200.0));
        BankAccount target = new BankAccount(new AccountNumber("2222222222"), new Money(50.0));
        InternalTransferService service = new InternalTransferService();

        service.transfer(source, target, new Money(100.0));

        assertEquals(100.0, source.getBalance().getAmount());
        assertEquals(150.0, target.getBalance().getAmount());
    }
}
