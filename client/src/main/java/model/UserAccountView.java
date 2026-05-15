package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserAccountView {
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();

    public UserAccountView(String username, String role) {
        this.username.set(username);
        this.role.set(role);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty roleProperty() {
        return role;
    }
}
