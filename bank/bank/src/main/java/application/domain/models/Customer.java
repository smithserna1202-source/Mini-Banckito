package application.domain.models;

import application.domain.valueobjects.Email;

public class Customer {
    private final String id;
    private final String fullName;
    private Email email;

    public Customer(String id, String fullName, Email email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
}
