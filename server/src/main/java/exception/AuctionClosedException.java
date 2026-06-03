package exception;

public class AuctionClosedException extends AuctionException {

    public AuctionClosedException() {
        super("Auction already closed");
    }

}
