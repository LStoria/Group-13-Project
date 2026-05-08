package repository;

import model.Auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAuctionRepository implements AuctionRepository {

    private final Map<Long, Auction> auctions = new ConcurrentHashMap<>();

    private long currentId = 1;

    @Override
    public void save(Auction auction) {

        if (auction.getId() == null) {
            auction.setId(currentId++);
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
        auctions.put(auction.getId(), auction);
    }

}
