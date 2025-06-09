package rizzerve.menuservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rizzerve.menuservice.config.MetricsConfig;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.factory.MenuItemFactory;
import rizzerve.menuservice.factory.MenuItemFactoryCreator;
import rizzerve.menuservice.model.Drink;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.repository.MenuRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final Counter menuItemCreatedCounter;
    private final Counter menuItemUpdatedCounter;
    private final Counter menuItemDeletedCounter;
    private final Counter menuItemRetrievedCounter;
    private final Timer menuOperationTimer;
    private final MeterRegistry meterRegistry;

    public MenuService(MenuRepository menuRepository, MeterRegistry meterRegistry) {
        this.menuRepository = menuRepository;
        this.meterRegistry = meterRegistry;
        this.menuItemCreatedCounter = Counter.builder("menu.items.created")
                .description("Number of menu items created")
                .register(meterRegistry);
        this.menuItemUpdatedCounter = Counter.builder("menu.items.updated")
                .description("Number of menu items updated")
                .register(meterRegistry);
        this.menuItemDeletedCounter = Counter.builder("menu.items.deleted")
                .description("Number of menu items deleted")
                .register(meterRegistry);
        this.menuItemRetrievedCounter = Counter.builder("menu.items.retrieved")
                .description("Number of menu items retrieved")
                .register(meterRegistry);
        this.menuOperationTimer = Timer.builder("menu.operation.duration")
                .description("Time taken for menu operations")
                .register(meterRegistry);
    }

    @Transactional
    public MenuItem addMenuItem(MenuType type, MenuItemRequest request) {
        long startTime = System.nanoTime();
        try {
            validateRequest(request);
            MenuItemFactory factory = MenuItemFactoryCreator.getFactory(type);
            MenuItem item = factory.createMenuItem(request);
            MenuItem savedItem = menuRepository.save(item);
            menuItemCreatedCounter.increment();
            return savedItem;
        } finally {
            Timer.builder("menu.operation.duration")
                    .tag("operation", "create")
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    public List<MenuItem> getAllMenuItems() {
        long startTime = System.nanoTime();
        try {
            List<MenuItem> items = menuRepository.findAll();
            menuItemRetrievedCounter.increment(items.size());
            return items;
        } finally {
            Timer.builder("menu.operation.duration")
                    .tag("operation", "getAll")
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    public MenuItem getMenuItemById(UUID id) {
        long startTime = System.nanoTime();
        try {
            MenuItem item = menuRepository.findById(id).orElse(null);
            if (item != null) {
                menuItemRetrievedCounter.increment();
            }
            return item;
        } finally {
            Timer.builder("menu.operation.duration")
                    .tag("operation", "getById")
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    @Transactional
    public MenuItem deleteMenuItem(UUID id) {
        long startTime = System.nanoTime();
        try {
            MenuItem item = menuRepository.findById(id).orElse(null);
            if (item == null) {
                return null;
            }
            menuRepository.deleteById(id);
            menuItemDeletedCounter.increment();
            return item;
        } finally {
            Timer.builder("menu.operation.duration")
                    .tag("operation", "delete")
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    @Transactional
    public MenuItem updateMenuItem(UUID id, MenuItemRequest request) {
        long startTime = System.nanoTime();
        try {
            validateRequest(request);
            MenuItem existingItem = menuRepository.findById(id).orElse(null);
            if (existingItem == null) {
                return null;
            }
            
            existingItem.setName(request.getName());
            existingItem.setDescription(request.getDescription());
            existingItem.setPrice(request.getPrice());
            existingItem.setImage(request.getImage());
            existingItem.setAvailable(request.getAvailable());
            
            if (existingItem instanceof Food food && request.getIsSpicy() != null) {
                food.setIsSpicy(request.getIsSpicy());
            } else if (existingItem instanceof Drink drink && request.getIsCold() != null) {
                drink.setIsCold(request.getIsCold());
            }
            
            MenuItem updatedItem = menuRepository.save(existingItem);
            menuItemUpdatedCounter.increment();
            return updatedItem;
        } finally {
            Timer.builder("menu.operation.duration")
                    .tag("operation", "update")
                    .register(meterRegistry)
                    .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Asynchronously get all menu items
     */
    @Async("taskExecutor")
    public CompletableFuture<List<MenuItem>> getAllMenuItemsAsync() {
        long startTime = System.nanoTime();
        try {
            List<MenuItem> menuItems = menuRepository.findAll();
            menuItemRetrievedCounter.increment(menuItems.size());
            return CompletableFuture.completedFuture(menuItems);
        } finally {
            menuOperationTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Asynchronously get a menu item by ID
     */
    @Async("taskExecutor")
    public CompletableFuture<MenuItem> getMenuItemByIdAsync(UUID id) {
        long startTime = System.nanoTime();
        try {
            MenuItem item = menuRepository.findById(id).orElse(null);
            if (item != null) {
                menuItemRetrievedCounter.increment();
            }
            return CompletableFuture.completedFuture(item);
        } finally {
            menuOperationTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Asynchronously add a new menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> addMenuItemAsync(MenuType type, MenuItemRequest request) {
        long startTime = System.nanoTime();
        try {
            validateRequest(request);
            MenuItemFactory factory = MenuItemFactoryCreator.getFactory(type);
            MenuItem item = factory.createMenuItem(request);
            MenuItem savedItem = menuRepository.save(item);
            menuItemCreatedCounter.increment();
            return CompletableFuture.completedFuture(savedItem);
        } catch (IllegalArgumentException e) {
            CompletableFuture<MenuItem> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        } finally {
            menuOperationTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Asynchronously update a menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> updateMenuItemAsync(UUID id, MenuItemRequest request) {
        long startTime = System.nanoTime();
        try {
            validateRequest(request);
            MenuItem existingItem = menuRepository.findById(id).orElse(null);
            if (existingItem == null) {
                return CompletableFuture.completedFuture(null);
            }
            
            existingItem.setName(request.getName());
            existingItem.setDescription(request.getDescription());
            existingItem.setPrice(request.getPrice());
            existingItem.setImage(request.getImage());
            existingItem.setAvailable(request.getAvailable());
            
            if (existingItem instanceof Food food && request.getIsSpicy() != null) {
                food.setIsSpicy(request.getIsSpicy());
            } else if (existingItem instanceof Drink drink && request.getIsCold() != null) {
                drink.setIsCold(request.getIsCold());
            }
            
            MenuItem updatedItem = menuRepository.save(existingItem);
            menuItemUpdatedCounter.increment();
            return CompletableFuture.completedFuture(updatedItem);
        } catch (IllegalArgumentException e) {
            CompletableFuture<MenuItem> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        } finally {
            menuOperationTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Asynchronously delete a menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> deleteMenuItemAsync(UUID id) {
        long startTime = System.nanoTime();
        try {
            MenuItem item = menuRepository.findById(id).orElse(null);
            if (item == null) {
                return CompletableFuture.completedFuture(null);
            }
            menuRepository.deleteById(id);
            menuItemDeletedCounter.increment();
            return CompletableFuture.completedFuture(item);
        } finally {
            menuOperationTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    private void validateRequest(MenuItemRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}