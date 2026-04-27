import java.util.ArrayList;
import java.util.List;

public class User extends Entity {

    private String username;
    private String passwordHash;
    private List<Role> roles = new ArrayList<>();

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    //Kiểm tra xem role này có phải là 1 trong những role của user không
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

}
