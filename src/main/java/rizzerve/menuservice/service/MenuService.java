package rizzerve.menuservice.service;

import org.springframework.stereotype.Service;
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

    public MenuItem addMenuItem(MenuType type, MenuItemRequest request) {
        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(type);
        MenuItem item = factory.createMenuItem(request);
        return menuRepository.save(item);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuRepository.findAll();
    }

    public MenuItem getMenuItemById(UUID id) {
        return menuRepository.findById(id);
    }

    public MenuItem deleteMenuItem(UUID id) {
        return menuRepository.delete(id);
    }

    public MenuItem updateMenuItem(UUID id, MenuItemRequest request) {
        MenuItem existingItem = menuRepository.findById(id);
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
}
