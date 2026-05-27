package repository.sqlite;

import database.DatabaseManager;
import model.user.User;
import repository.repointerface.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {

    @Override
    public void save(User user) {

        String sql = """
            INSERT INTO users(
                username,
                email,
                password_hash
            )
            VALUES(?,?,?)
        """;

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<User> findByUsername(
            String username
    ) {

        String sql =
                "SELECT * FROM users WHERE username=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );

                return Optional.of(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public Optional<User> findById(Long id) {

        String sql =
                "SELECT * FROM users WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );

                user.setId(rs.getLong("id"));

                return Optional.of(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {

        String sql =
                "SELECT 1 FROM users WHERE username=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public boolean existsById(Long id) {

        String sql =
                "SELECT 1 FROM users WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }



    @Override
    public void update(User user) {

        String sql = """
        UPDATE users
        SET username=?,
            email=?,
            password_hash=?
        WHERE id=?
    """;

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setLong(4, user.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {

        String sql =
                "DELETE FROM users WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
