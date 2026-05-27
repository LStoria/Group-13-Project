package repository.repointerface;

import model.auction.BidTransaction;

import java.util.List;
import java.util.Optional;

public interface BidTransactionRepository {

    void save(BidTransaction bid);

    Optional<BidTransaction> findById(Long id);

    List<BidTransaction> findAll();

    List<BidTransaction> findByAuctionId(Long auctionId);

    List<BidTransaction> findByBidderId(Long bidderId);

    void update(BidTransaction bid);

    void delete(Long id);

}
