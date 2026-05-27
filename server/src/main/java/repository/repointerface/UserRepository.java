package repository.repointerface;

import model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();

    boolean existsByUsername(String username);
    boolean existsById(Long id);

    void update(User user);

    void delete(Long id);

}
