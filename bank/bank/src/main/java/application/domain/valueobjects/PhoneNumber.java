package application.domain.valueobjects;

public class PhoneNumber {
    private final String number;

    public PhoneNumber(String number) {
        if (number == null || !number.matches("\\d{7,15}")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
