package repository.memory;

import model.auction.BidTransaction;
import repository.repointerface.BidTransactionRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryBidTransactionRepository implements BidTransactionRepository {

    private final Map<Long, BidTransaction> bids = new ConcurrentHashMap<>();

    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public void save(BidTransaction bid) {

        if (bid == null) {
            throw new IllegalArgumentException("Bid cannot be null");
        }

        if (bid.getId() == null) {
            bid.setId(currentId.getAndIncrement());
        }

        bids.put(bid.getId(), bid);
    }

    @Override
    public Optional<BidTransaction> findById(Long id) {
        return Optional.ofNullable(bids.get(id));
    }

    @Override
    public List<BidTransaction> findAll() {
        return bids.values()
                .stream()
                .sorted(Comparator.comparing(BidTransaction::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BidTransaction> findByAuctionId(Long auctionId) {

        return bids.values()
                .stream()
                .filter(bid -> bid.getAuctionId().equals(auctionId))
                .sorted(Comparator.comparing(BidTransaction::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BidTransaction> findByBidderId(Long bidderId) {

        return bids.values()
                .stream()
                .filter(bid -> bid.getBidderId().equals(bidderId))
                .sorted(Comparator.comparing(BidTransaction::getId))
                .collect(Collectors.toList());
    }

    @Override
    public void update(BidTransaction bid) {

        if (bid == null || bid.getId() == null) {
            throw new IllegalArgumentException("Bid or Bid ID cannot be null");
        }

        if (!bids.containsKey(bid.getId())) {
            throw new IllegalArgumentException("Bid not found");
        }

        bids.put(bid.getId(), bid);
    }

    @Override
    public void delete(Long id) {
        bids.remove(id);
    }

}
