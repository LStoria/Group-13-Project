package repository.memory;

import model.item.Item;
import repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();

    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public void save(Item item) {

        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (item.getId() == null) {
            item.setId(currentId.getAndIncrement());
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

        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("Item or ID cannot be null");
        }

        if (!items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Item not found");
        }

        items.put(item.getId(), item);
    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }

}
