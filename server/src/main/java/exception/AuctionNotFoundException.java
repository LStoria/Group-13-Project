package exception;

public class AuctionNotFoundException extends AuctionException {

    public AuctionNotFoundException(Long id) {
        super("Auction not found: " + id);
    }

}
