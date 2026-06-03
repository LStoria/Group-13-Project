package repository.sqlite;

import database.DatabaseManager;

import model.auction.Auction;
import model.auction.BidTransaction;
import model.user.User;

import repository.repointerface.BidTransactionRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteBidTransactionRepository implements BidTransactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteBidTransactionRepository.class);

    private final SQLiteAuctionRepository auctionRepository =
            new SQLiteAuctionRepository();

    private final SQLiteUserRepository userRepository =
            new SQLiteUserRepository();

    @Override
    public void save(BidTransaction bid) {

        String sql = """
            INSERT INTO bid_transactions(
                auction_id,
                bidder_id,
                amount,
                bid_time
            )
            VALUES(?,?,?,?)
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
                    bid.getAuctionId()
            );

            stmt.setLong(
                    2,
                    bid.getBidderId()
            );

            stmt.setDouble(
                    3,
                    bid.getAmount()
            );

            stmt.setString(
                    4,
                    bid.getBidTime().toString()
            );

            stmt.executeUpdate();

            ResultSet keys =
                    stmt.getGeneratedKeys();

            if (keys.next()) {

                bid.setId(
                        keys.getLong(1)
                );
            }

        } catch (SQLException e) {
            logger.error("SQLiteBidTransactionRepository.save failed", e);
        }
    }

    @Override
    public Optional<BidTransaction> findById(Long id) {

        String sql =
                "SELECT * FROM bid_transactions WHERE id=?";

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
                        createBidFromResultSet(rs)
                );
            }

        } catch (SQLException e) {
            logger.error("SQLiteBidTransactionRepository.findById failed", e);
        }

        return Optional.empty();
    }

    @Override
    public List<BidTransaction> findAll() {

        List<BidTransaction> bids =
                new ArrayList<>();

        String sql =
                "SELECT * FROM bid_transactions";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                bids.add(
                        createBidFromResultSet(rs)
                );
            }

        } catch (SQLException e) {
            logger.error("SQLiteBidTransactionRepository.findAll failed", e);
        }

        return bids;
    }

    @Override
    public List<BidTransaction> findByAuctionId(Long auctionId) {

        return findAll().stream()
                .filter(b -> b.getAuctionId().equals(auctionId))
                .toList();
    }

    @Override
    public List<BidTransaction> findByBidderId(Long bidderId) {

        return findAll().stream()
                .filter(b -> b.getBidderId().equals(bidderId))
                .toList();
    }

    @Override
    public void update(BidTransaction bid) {

        String sql = """
            UPDATE bid_transactions
            SET amount=?
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
                    bid.getAmount()
            );

            stmt.setLong(
                    2,
                    bid.getId()
            );

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("SQLiteBidTransactionRepository.update failed", e);
        }
    }

    @Override
    public void delete(Long id) {

        String sql =
                "DELETE FROM bid_transactions WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setLong(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("SQLiteBidTransactionRepository.delete failed", e);
        }
    }

    private BidTransaction createBidFromResultSet(ResultSet rs)
            throws SQLException {

        Auction auction =
                auctionRepository.findById(
                        rs.getLong("auction_id")
                ).orElse(null);

        User bidder =
                userRepository.findById(
                        rs.getLong("bidder_id")
                ).orElse(null);

        BidTransaction bid =
                new BidTransaction(
                        auction,
                        bidder,
                        rs.getDouble("amount"),
                        LocalDateTime.parse(
                                rs.getString("bid_time")
                        )
                );

        bid.setId(
                rs.getLong("id")
        );

        return bid;
    }

}

