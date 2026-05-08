package repository;

import model.Auction;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {

    void save(Auction auction);

    Optional<Auction> findById(Long id);

    List<Auction> findAll();

    void delete(Long id);

    void update(Auction auction);

}
