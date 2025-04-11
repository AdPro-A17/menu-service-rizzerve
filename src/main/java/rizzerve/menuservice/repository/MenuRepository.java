package rizzerve.menuservice.repository;

import org.springframework.stereotype.Repository;
import rizzerve.menuservice.model.MenuItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MenuRepository {

    private final Map<UUID, MenuItem> storage = new ConcurrentHashMap<>();

    public MenuItem save(MenuItem item) {
        storage.put(item.getId(), item);
        return item;
    }

    public List<MenuItem> findAll() {
        return new ArrayList<>(storage.values());
    }

    public MenuItem findById(UUID id) {
        return storage.get(id);
    }

    public MenuItem delete(UUID id) {
        return storage.remove(id);
    }
}
