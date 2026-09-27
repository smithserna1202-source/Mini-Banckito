package application.domain.valueobjects;

public class AccountNumber {
    private final String value;

    public AccountNumber(String value) {
        if (value == null || !value.matches("\\d{10}")) {
            throw new IllegalArgumentException("Account number must be 10 digits");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
