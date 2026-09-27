package application.domain.models;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BankAccountTest {
    @Test
    void testDeposit() {
        BankAccount account = new BankAccount(new AccountNumber("1234567890"), new Money(100.0));
        account.deposit(new Money(50.0));
        assertEquals(150.0, account.getBalance().getAmount());
    }
}
