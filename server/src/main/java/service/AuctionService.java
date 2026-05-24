package service;

import model.*;

import model.items.User;
import repository.AuctionRepository;
import repository.BidTransactionRepository;

import java.time.LocalDateTime;

public class AuctionService {

    private final AuctionRepository auctionRepository;

    private final BidTransactionRepository bidRepository;

    public AuctionService(AuctionRepository auctionRepository,
                          BidTransactionRepository bidRepository) {

        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    public synchronized void placeBid(
            Long auctionId,
            User bidder,
            double amount
    ) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Auction not found"));

        validateAuction(auction);

        validateBidAmount(auction, amount);

        BidTransaction bid = new BidTransaction(
                auction,
                bidder,
                amount,
                LocalDateTime.now()
        );

        auction.setCurrentPrice(amount);

        auction.setHighestBidder(bidder);

        auction.getBids().add(bid);

        bidRepository.save(bid);

        auctionRepository.update(auction);
    }

    private void validateAuction(Auction auction) {

        if (auction.getStatus() == AuctionStatus.FINISHED ||
                auction.getStatus() == AuctionStatus.CANCELED) {

            throw new IllegalStateException(
                    "Auction already closed");
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {

            auction.setStatus(AuctionStatus.FINISHED);

            throw new IllegalStateException(
                    "Auction expired");
        }
    }

    private void validateBidAmount(
            Auction auction,
            double amount
    ) {

        if (amount <= auction.getCurrentPrice()) {

            throw new IllegalArgumentException(
                    "Bid must be higher than current price");
        }
    }

    public void startAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow();

        auction.setStatus(AuctionStatus.RUNNING);

        auctionRepository.update(auction);
    }

    public void finishAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow();

        auction.setStatus(AuctionStatus.FINISHED);

        auctionRepository.update(auction);
    }

}
