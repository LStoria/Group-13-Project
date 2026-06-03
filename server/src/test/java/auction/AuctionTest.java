package auction;

import model.auction.Auction;
import model.item.Item;
import model.user.User;
import model.auction.BidTransaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void placeBidSuccess() {

        User seller =
                new User(
                        "seller",
                        "s@test.com",
                        "123"
                );

        User bidder =
                new User(
                        "bidder",
                        "b@test.com",
                        "123"
                );

        Item item =
                new TestItem(
                        "Laptop",
                        seller,
                        1000,
                        1000
                );

        Auction auction =
                new Auction(
                        item,
                        1000,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1)
                );

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

}