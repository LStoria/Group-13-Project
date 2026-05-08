package repository;

import model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    void save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAll();

    void delete(Long id);

    void update(Item item);

}
