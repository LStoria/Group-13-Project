package model.items;

import role.Role;

import java.util.ArrayList;
import java.util.List;

public class User extends Entity {

    private String username;
    private String email;
    private String passwordHash;
    private List<Role> roles = new ArrayList<>();

    public User(String username,String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    //Kiểm tra xem role này có phải là 1 trong những role của user không
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

}
