package rizzerve.menuservice.service;

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
        
        if (existingItem instanceof Food && request.getIsSpicy() != null) {
            ((Food) existingItem).setIsSpicy(request.getIsSpicy());
        } else if (existingItem instanceof Drink && request.getIsCold() != null) {
            ((Drink) existingItem).setIsCold(request.getIsCold());
        }
        
        return menuRepository.save(existingItem);
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
