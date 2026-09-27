package application.domain.valueobjects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    void testMoneyCreation() {
        Money money = new Money(100.0);
        assertEquals(100.0, money.getAmount());
    }
}
