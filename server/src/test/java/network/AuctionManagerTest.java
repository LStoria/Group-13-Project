package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionManagerTest {
    @Test
    void sellerLoginReturnsSellerRole() {
        JsonObject response = AuctionManager.authenticate("seller", "seller");

        assertEquals("SUCCESS", response.get("status").getAsString());
        assertEquals("SELLER", response.get("role").getAsString());
    }

    @Test
    void loginRejectsWrongPassword() {
        JsonObject response = AuctionManager.authenticate("seller", "wrong-password");

        assertEquals("ERROR", response.get("status").getAsString());
    }

    @Test
    void registerCreatesNewBidderAccount() {
        String username = "test_bidder_" + System.nanoTime();
        JsonObject register = AuctionManager.register(username, "secret", "BIDDER");
        JsonObject login = AuctionManager.authenticate(username, "secret");

        assertEquals("SUCCESS", register.get("status").getAsString());
        assertEquals("SUCCESS", login.get("status").getAsString());
        assertEquals("BIDDER", login.get("role").getAsString());
    }

    @Test
    void createItemAddsItemToSellerList() {
        JsonObject response = AuctionManager.createItem("Test Camera", "Electronics", 250.0, "seller");

        assertEquals("SUCCESS", response.get("status").getAsString());

        JsonArray sellerItems = JsonParser.parseString(AuctionManager.getItemsJson("seller")).getAsJsonArray();
        boolean found = false;
        for (int i = 0; i < sellerItems.size(); i++) {
            JsonObject item = sellerItems.get(i).getAsJsonObject();
            if ("Test Camera".equals(item.get("name").getAsString())) {
                found = true;
                assertEquals("Electronics", item.get("type").getAsString());
                assertEquals("ACTIVE", item.get("status").getAsString());
            }
        }

        assertTrue(found);
    }

    @Test
    void bidMustBeHigherThanCurrentPrice() {
        JsonObject response = AuctionManager.createItem("Test Watch", "Other", 100.0, "seller");
        int itemId = response.getAsJsonObject("item").get("id").getAsInt();

        assertFalse(AuctionManager.updateBid((long)itemId, 90.0, "bidder"));
        assertTrue(AuctionManager.updateBid((long)itemId, 120.0, "bidder"));
    }
}
