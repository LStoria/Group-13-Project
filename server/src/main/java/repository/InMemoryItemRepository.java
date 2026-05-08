package repository;

import model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();

    private long currentId = 1;

    @Override
    public void save(Item item) {

        if (item.getId() == null) {
            item.setId(currentId++);
        }

        items.put(item.getId(), item);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }

    @Override
    public void update(Item item) {
        items.put(item.getId(), item);
    }

}
