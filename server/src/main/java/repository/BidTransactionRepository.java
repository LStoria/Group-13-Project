package repository;

import model.BidTransaction;

import java.util.List;
import java.util.Optional;

public interface BidTransactionRepository {

    void save(BidTransaction bid);

    Optional<BidTransaction> findById(Long id);

    List<BidTransaction> findAll();

    void delete(Long id);

}
