package exception;

public class AuctionExpiredException extends AuctionException {

    public AuctionExpiredException() {
        super("Auction expired");
    }

}
