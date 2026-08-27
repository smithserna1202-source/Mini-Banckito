package application.domain.models;

public class Customer {
    private String id;
    private String name;
    private String email;

    public Customer(String id, String name, String email) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del cliente no puede estar vacío.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
