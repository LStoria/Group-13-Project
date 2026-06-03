package auction;

import model.auction.Auction;
import model.item.Item;
import model.user.User;
import org.junit.jupiter.api.Test;
import repository.sqlite.SQLiteAuctionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteAuctionRepositoryTest {

    //Test lưu và đọc auction

    @Test
    void saveAndFindById() {

        SQLiteAuctionRepository repository =
                new SQLiteAuctionRepository();

        User seller =
                new User(
                        "seller_test",
                        "seller@test.com",
                        "123"
                );

        seller.setId(1L);

        Item item = new TestItem(
                "Laptop",
                seller,
                1000,
                1000
        );

        item.setId(1L);

        Auction auction =
                new Auction(
                        item,
                        1000,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)
                );

        repository.save(auction);

        Optional<Auction> result =
                repository.findById(
                        auction.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                auction.getId(),
                result.get().getId()
        );
    }


    //Tést update Auction

    @Test
    void updateAuction() {

        SQLiteAuctionRepository repository =
                new SQLiteAuctionRepository();

        User seller =
                new User(
                        "seller_test",
                        "seller@test.com",
                        "123"
                );

        seller.setId(1L);

        Item item = new TestItem(
                "Phone",
                seller,
                500,
                500
        );

        item.setId(1L);

        Auction auction =
                new Auction(
                        item,
                        500,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)
                );

        repository.save(auction);

        auction.setCurrentPrice(800);

        repository.update(auction);

        Auction loaded =
                repository.findById(
                        auction.getId()
                ).orElseThrow();

        assertEquals(
                800,
                loaded.getCurrentPrice()
        );
    }


    //Test delete

    @Test
    void deleteAuction() {

        SQLiteAuctionRepository repository =
                new SQLiteAuctionRepository();

        User seller =
                new User(
                        "seller_test",
                        "seller@test.com",
                        "123"
                );

        seller.setId(1L);

        Item item = new TestItem(
                "Tablet",
                seller,
                300,
                300
        );

        item.setId(1L);

        Auction auction =
                new Auction(
                        item,
                        300,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)
                );

        repository.save(auction);

        Long id = auction.getId();

        repository.delete(id);

        assertFalse(
                repository.findById(id)
                        .isPresent()
        );
    }


}