package auction;

import exception.AuctionExpiredException;
import exception.InvalidBidException;
import model.auction.Auction;
import model.auction.AuctionStatus;
import model.auction.BidTransaction;
import model.item.Electronics;
import model.item.Item;
import model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Auction auction;

    private User seller;
    private User bidder;

    @BeforeEach
    void setUp() {

        seller = new User(
                "seller",
                "seller@test.com",
                "123",
                "SELLER"
        );

        bidder = new User(
                "bidder",
                "bidder@test.com",
                "123",
                "BIDDER"
        );

        Item item =
                new Electronics(
                        "Laptop",
                        seller,
                        1000,
                        1000
                );

        auction =
                new Auction(
                        item,
                        1000,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1)
                );
    }

    @Test
    void placeBid_ShouldUpdatePriceAndHighestBidder() {

        BidTransaction bid =
                new BidTransaction(
                        auction,
                        bidder,
                        1200,
                        LocalDateTime.now()
                );

        auction.placeBid(bid);

        assertEquals(
                1200,
                auction.getCurrentPrice()
        );

        assertEquals(
                bidder,
                auction.getHighestBidder()
        );

        assertEquals(
                1,
                auction.getBids().size()
        );
    }

    @Test
    void placeBid_LowerPrice_ShouldThrowException() {

        BidTransaction bid =
                new BidTransaction(
                        auction,
                        bidder,
                        900,
                        LocalDateTime.now()
                );

        assertThrows(
                InvalidBidException.class,
                () -> auction.placeBid(bid)
        );
    }

    @Test
    void placeBid_EqualPrice_ShouldThrowException() {

        BidTransaction bid =
                new BidTransaction(
                        auction,
                        bidder,
                        1000,
                        LocalDateTime.now()
                );

        assertThrows(
                InvalidBidException.class,
                () -> auction.placeBid(bid)
        );
    }

    @Test
    void placeBid_WhenAuctionExpired_ShouldThrowException() {

        Item item =
                new Electronics(
                        "Phone",
                        seller,
                        500,
                        500
                );

        Auction expiredAuction =
                new Auction(
                        item,
                        500,
                        LocalDateTime.now().minusHours(2),
                        LocalDateTime.now().minusMinutes(1)
                );

        BidTransaction bid =
                new BidTransaction(
                        expiredAuction,
                        bidder,
                        600,
                        LocalDateTime.now()
                );

        assertThrows(
                AuctionExpiredException.class,
                () -> expiredAuction.placeBid(bid)
        );

        assertEquals(
                AuctionStatus.FINISHED,
                expiredAuction.getStatus()
        );
    }

    @Test
    void placeBid_WhenFinished_ShouldThrowException() {

        auction.setStatus(
                AuctionStatus.FINISHED
        );

        BidTransaction bid =
                new BidTransaction(
                        auction,
                        bidder,
                        1500,
                        LocalDateTime.now()
                );

        assertThrows(
                IllegalStateException.class,
                () -> auction.placeBid(bid)
        );
    }
}