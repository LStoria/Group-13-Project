package repository.sqlite;

import database.DatabaseManager;

import model.auction.Auction;
import model.auction.AuctionStatus;
import model.auction.BidTransaction;
import model.item.Item;
import model.user.User;

import repository.repointerface.AuctionRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteAuctionRepository implements AuctionRepository {

    private final SQLiteItemRepository itemRepository =
            new SQLiteItemRepository();

    private final SQLiteUserRepository userRepository =
            new SQLiteUserRepository();

    @Override
    public void save(Auction auction) {

        String sql = """
            INSERT INTO auctions(
                item_id,
                current_price,
                highest_bidder_id,
                start_time,
                end_time,
                status
            )
            VALUES(?,?,?,?,?,?)
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

            stmt.setLong(
                    1,
                    auction.getItem().getId()
            );

            stmt.setDouble(
                    2,
                    auction.getCurrentPrice()
            );

            if (auction.getHighestBidder() != null) {

                stmt.setLong(
                        3,
                        auction.getHighestBidder().getId()
                );

            } else {

                stmt.setNull(
                        3,
                        Types.INTEGER
                );
            }

            stmt.setString(
                    4,
                    auction.getStartTime().toString()
            );

            stmt.setString(
                    5,
                    auction.getEndTime().toString()
            );

            stmt.setString(
                    6,
                    auction.getStatus().name()
            );

            stmt.executeUpdate();

            ResultSet keys =
                    stmt.getGeneratedKeys();

            if (keys.next()) {

                auction.setId(
                        keys.getLong(1)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Auction> findById(Long id) {

        String sql =
                "SELECT * FROM auctions WHERE id=?";

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

                return Optional.of(
                        createAuctionFromResultSet(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Auction> findAll() {

        List<Auction> auctions =
                new ArrayList<>();

        String sql =
                "SELECT * FROM auctions";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                auctions.add(
                        createAuctionFromResultSet(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return auctions;
    }

    @Override
    public void delete(Long id) {

        String sql =
                "DELETE FROM auctions WHERE id=?";

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
    public void update(Auction auction) {

        String sql = """
            UPDATE auctions
            SET current_price=?,
                highest_bidder_id=?,
                status=?
            WHERE id=?
        """;

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setDouble(
                    1,
                    auction.getCurrentPrice()
            );

            if (auction.getHighestBidder() != null) {

                stmt.setLong(
                        2,
                        auction.getHighestBidder().getId()
                );

            } else {

                stmt.setNull(
                        2,
                        Types.INTEGER
                );
            }

            stmt.setString(
                    3,
                    auction.getStatus().name()
            );

            stmt.setLong(
                    4,
                    auction.getId()
            );

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsById(Long id) {

        return findById(id).isPresent();
    }

    private Auction createAuctionFromResultSet(ResultSet rs)
            throws SQLException {

        Long itemId =
                rs.getLong("item_id");

        Item item =
                itemRepository.findById(itemId)
                        .orElse(null);

        Auction auction =
                new Auction(
                        item,
                        rs.getDouble("current_price"),
                        LocalDateTime.parse(
                                rs.getString("start_time")
                        ),
                        LocalDateTime.parse(
                                rs.getString("end_time")
                        )
                );

        auction.setId(
                rs.getLong("id")
        );

        auction.setCurrentPrice(
                rs.getDouble("current_price")
        );

        Long bidderId =
                rs.getLong("highest_bidder_id");

        if (!rs.wasNull()) {

            User bidder =
                    userRepository.findById(bidderId)
                            .orElse(null);

            auction.setHighestBidder(bidder);
        }

        auction.setStatus(
                AuctionStatus.valueOf(
                        rs.getString("status")
                )
        );

        return auction;
    }

    @Override
    public void placeBid(
            Long auctionId,
            BidTransaction bid
    ) {

        Auction auction =
                findById(auctionId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Auction not found"
                                        )
                        );

        auction.placeBid(bid);

        update(auction);
    }

}
