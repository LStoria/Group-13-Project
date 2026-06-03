package repository.memory;

import model.auction.Auction;
import model.auction.BidTransaction;
import repository.repointerface.AuctionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAuctionRepository implements AuctionRepository {

    private final Map<Long, Auction> auctions = new ConcurrentHashMap<>();

    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public void save(Auction auction) {

        if (auction == null) {
            throw new IllegalArgumentException("Auction cannot be null");
        }

        if (auction.getId() == null) {
            auction.setId(currentId.getAndIncrement());
        }

        auctions.put(auction.getId(), auction);
    }

    @Override
    public Optional<Auction> findById(Long id) {
        return Optional.ofNullable(auctions.get(id));
    }

    @Override
    public List<Auction> findAll() {
        return new ArrayList<>(auctions.values());
    }

    @Override
    public void delete(Long id) {
        auctions.remove(id);
    }

    @Override
    public void update(Auction auction) {

        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction or ID cannot be null");
        }

        if (!auctions.containsKey(auction.getId())) {
            throw new IllegalArgumentException("Auction not found");
        }

        auctions.put(auction.getId(), auction);
    }

    public boolean existsById(Long id) {
        return auctions.containsKey(id);
    }

    @Override
    public void placeBid(
            Long auctionId,
            BidTransaction bid
    ) {

        Auction auction =
                auctions.get(auctionId);

        if (auction == null) {

            throw new IllegalArgumentException(
                    "Auction not found"
            );
        }

        auction.placeBid(bid);
    }

}
