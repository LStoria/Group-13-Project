package repository.sqlite;

import database.DatabaseManager;

import model.item.Art;
import model.item.Electronics;
import model.item.Item;
import model.item.Vehicle;

import repository.repointerface.ItemRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteItemRepository implements ItemRepository {

    @Override
    public void save(Item item) {

        String sql = """
            INSERT INTO items(
                item_type,
                name,
                seller_id,
                start_price,
                current_price
            )
            VALUES(?,?,?,?,?)
        """;

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            stmt.setString(
                    1,
                    item.getType()
            );

            stmt.setString(
                    2,
                    item.getName()
            );

            if (item.getSeller() != null) {

                stmt.setLong(
                        3,
                        item.getSeller().getId()
                );

            } else {

                stmt.setNull(
                        3,
                        Types.INTEGER
                );
            }

            stmt.setDouble(
                    4,
                    item.getStartPrice()
            );

            stmt.setDouble(
                    5,
                    item.getCurrentPrice()
            );

            stmt.executeUpdate();

            ResultSet keys =
                    stmt.getGeneratedKeys();

            if (keys.next()) {

                item.setId(
                        keys.getLong(1)
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    @Override
    public Optional<Item> findById(Long id) {

        String sql =
                "SELECT * FROM items WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                Item item =
                        createItemFromResultSet(rs);

                return Optional.of(item);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return Optional.empty();

    }

    @Override
    public List<Item> findAll() {

        List<Item> items =
                new ArrayList<>();

        String sql =
                "SELECT * FROM items";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                Item item =
                        createItemFromResultSet(rs);

                items.add(item);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return items;
    }

    @Override
    public void delete(Long id) {

        String sql =
                "DELETE FROM items WHERE id=?";

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

    @Override
    public void update(Item item) {

        String sql = """
            UPDATE items
            SET item_type=?,
                name=?,
                seller_id=?,
                start_price=?,
                current_price=?
            WHERE id=?
        """;

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    item.getType()
            );

            stmt.setString(
                    2,
                    item.getName()
            );

            if (item.getSeller() != null) {

                stmt.setLong(
                        3,
                        item.getSeller().getId()
                );

            } else {

                stmt.setNull(
                        3,
                        Types.INTEGER
                );
            }

            stmt.setDouble(
                    4,
                    item.getStartPrice()
            );

            stmt.setDouble(
                    5,
                    item.getCurrentPrice()
            );

            stmt.setLong(
                    6,
                    item.getId()
            );

            stmt.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        }

    @Override
    public boolean existsById(Long id) {

        String sql =
                "SELECT 1 FROM items WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
    }


    private Item createItemFromResultSet(
            ResultSet rs
    ) throws SQLException {

        String type =
                rs.getString("item_type");

        Item item;

        switch (type) {

            case "ART" ->

                    item = new Art(
                            rs.getString("name"),
                            null,
                            rs.getDouble("start_price"),
                            rs.getDouble("current_price"),
                            "Unknown"
                    );


            case "VEHICLE" ->

                    item = new Vehicle(
                            rs.getString("name"),
                            null,
                            rs.getDouble("start_price"),
                            rs.getDouble("current_price"),
                            "Unknown",
                            "Unknown"
                    );
            case "ELECTRONICS" ->

                    item = new Electronics(
                            rs.getString("name"),
                            null,
                            rs.getDouble("start_price"),
                            rs.getDouble("current_price"),
                            "Unknown"
                    );

            default ->
                    throw new SQLException(
                            "Unknown item type: "
                                    + type
                    );
        }

        item.setId(
                rs.getLong("id")
        );

        item.setCurrentPrice(
                rs.getDouble("current_price")
        );

        return item;
    }

}

