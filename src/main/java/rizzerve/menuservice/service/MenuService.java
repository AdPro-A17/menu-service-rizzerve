package rizzerve.menuservice.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional
    public MenuItem addMenuItem(MenuType type, MenuItemRequest request) {
        validateRequest(request);
        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(type);
        MenuItem item = factory.createMenuItem(request);
        return menuRepository.save(item);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuRepository.findAll();
    }

    public MenuItem getMenuItemById(UUID id) {
        return menuRepository.findById(id).orElse(null);
    }

    @Transactional
    public MenuItem deleteMenuItem(UUID id) {
        MenuItem item = menuRepository.findById(id).orElse(null);
        if (item == null) {
            return null;
        }
        menuRepository.deleteById(id);
        return item;
    }

    @Transactional
    public MenuItem updateMenuItem(UUID id, MenuItemRequest request) {
        validateRequest(request);
        MenuItem existingItem = menuRepository.findById(id).orElse(null);
        if (existingItem == null) {
            return null;
        }
        
        existingItem.setName(request.getName());
        existingItem.setDescription(request.getDescription());
        existingItem.setPrice(request.getPrice());
        existingItem.setImage(request.getImage());
        
        if (existingItem instanceof Food && request.getIsSpicy() != null) {
            ((Food) existingItem).setIsSpicy(request.getIsSpicy());
        } else if (existingItem instanceof Drink && request.getIsCold() != null) {
            ((Drink) existingItem).setIsCold(request.getIsCold());
        }
        
        return menuRepository.save(existingItem);
    }

    /**
     * Asynchronously get all menu items
     */
    @Async("taskExecutor")
    public CompletableFuture<List<MenuItem>> getAllMenuItemsAsync() {
        List<MenuItem> menuItems = menuRepository.findAll();
        return CompletableFuture.completedFuture(menuItems);
    }

    /**
     * Asynchronously get a menu item by ID
     */
    @Async("taskExecutor")
    public CompletableFuture<MenuItem> getMenuItemByIdAsync(UUID id) {
        return CompletableFuture.completedFuture(menuRepository.findById(id).orElse(null));
    }

    /**
     * Asynchronously add a new menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> addMenuItemAsync(MenuType type, MenuItemRequest request) {
        try {
            validateRequest(request);
            MenuItemFactory factory = MenuItemFactoryCreator.getFactory(type);
            MenuItem item = factory.createMenuItem(request);
            MenuItem savedItem = menuRepository.save(item);
            return CompletableFuture.completedFuture(savedItem);
        } catch (IllegalArgumentException e) {
            CompletableFuture<MenuItem> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously update a menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> updateMenuItemAsync(UUID id, MenuItemRequest request) {
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
            
            if (existingItem instanceof Food && request.getIsSpicy() != null) {
                ((Food) existingItem).setIsSpicy(request.getIsSpicy());
            } else if (existingItem instanceof Drink && request.getIsCold() != null) {
                ((Drink) existingItem).setIsCold(request.getIsCold());
            }
            
            MenuItem updatedItem = menuRepository.save(existingItem);
            return CompletableFuture.completedFuture(updatedItem);
        } catch (IllegalArgumentException e) {
            CompletableFuture<MenuItem> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously delete a menu item
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<MenuItem> deleteMenuItemAsync(UUID id) {
        MenuItem item = menuRepository.findById(id).orElse(null);
        if (item == null) {
            return CompletableFuture.completedFuture(null);
        }
        menuRepository.deleteById(id);
        return CompletableFuture.completedFuture(item);
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
