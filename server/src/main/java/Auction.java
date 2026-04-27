import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {

    private Item item;
    private List<BidTransaction> bids = new ArrayList<>();

    public Auction(Item item) {
        this.item = item;
    }

    public double getCurrentPrice() {
        return item.getCurrentPrice();
    }

    public void addBid(BidTransaction bid) {
        bids.add(bid);
        item.setCurrentPrice(bid.getAmount());
    }

}
