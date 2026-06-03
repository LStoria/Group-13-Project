package model.auction;

import model.item.Entity;
import model.item.Item;
import model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {

    private Item item;
    private double currentPrice;
    private User highestBidder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private final List<BidTransaction> bids = new ArrayList<>();

    public Auction(Item item,
                   double startingPrice,
                   LocalDateTime startTime,
                   LocalDateTime endTime) {

        this.item = item;
        this.currentPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;

        this.status = AuctionStatus.OPEN;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(User highestBidder) {
        this.highestBidder = highestBidder;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Item getItem() {
        return item;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<BidTransaction> getBids() {
        return bids;
    }


    public synchronized void placeBid(BidTransaction bid) {

        if (status == AuctionStatus.FINISHED
                || status == AuctionStatus.CANCELED) {

            throw new IllegalStateException(
                    "Auction already closed"
            );
        }

        if (LocalDateTime.now().isAfter(endTime)) {

            status = AuctionStatus.FINISHED;

            throw new IllegalStateException(
                    "Auction expired"
            );
        }

        if (bid.getAmount() <= currentPrice) {

            throw new IllegalArgumentException(
                    "Bid must be higher than current price"
            );
        }

        currentPrice = bid.getAmount();

        highestBidder = bid.getBidder();

        bids.add(bid);
    }

}
