package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                Statement stmt =
                        conn.createStatement()
        ) {

            conn.setAutoCommit(true);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT NOT NULL,
                    password_hash TEXT NOT NULL
                )
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
