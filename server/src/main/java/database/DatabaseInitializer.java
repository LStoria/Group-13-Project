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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS items(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    seller_id INTEGER,
                    start_price REAL NOT NULL,
                    current_price REAL NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS auctions(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    current_price REAL NOT NULL,
                    highest_bidder_id INTEGER,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    status TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bid_transactions(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    auction_id INTEGER NOT NULL,
                    bidder_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    bid_time TEXT NOT NULL
                )
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

}
