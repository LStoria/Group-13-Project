package repository;

import model.BidTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBidTransactionRepository implements BidTransactionRepository {

    private final Map<Long, BidTransaction> bids = new ConcurrentHashMap<>();

    private long currentId = 1;

    @Override
    public void save(BidTransaction bid) {

        if (bid.getId() == null) {
            bid.setId(currentId++);
        }

        bids.put(bid.getId(), bid);
    }

    @Override
    public Optional<BidTransaction> findById(Long id) {
        return Optional.ofNullable(bids.get(id));
    }

    @Override
    public List<BidTransaction> findAll() {
        return new ArrayList<>(bids.values());
    }

    @Override
    public void delete(Long id) {
        bids.remove(id);
    }

}
