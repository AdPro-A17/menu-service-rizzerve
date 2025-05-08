package rizzerve.menuservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.repository.MenuRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        MenuRepository repository = new MenuRepository();
        this.menuService = new MenuService(repository);
    }

    @Test
    void testAddNewMenuItem() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Mie Goreng");
        request.setDescription("Fried noodle");
        request.setPrice(25000.0);
        request.setIsSpicy(true);

        MenuItem savedItem = menuService.addMenuItem(MenuType.FOOD, request);

        assertNotNull(savedItem.getId());
        assertEquals("Mie Goreng", savedItem.getName());
        assertEquals(25000.0, savedItem.getPrice());
        assertInstanceOf(Food.class, savedItem);
    }

    @Test
    void testGetAllMenuItems() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Nasi Goreng");
        request.setDescription("Fried rice");
        request.setPrice(27000.0);
        request.setIsSpicy(true);

        menuService.addMenuItem(MenuType.FOOD, request);

        List<MenuItem> items = menuService.getAllMenuItems();
        assertEquals(1, items.size());
    }

    @Test
    void testGetMenuItemById() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Teh Manis");
        request.setDescription("Sweet tea");
        request.setPrice(7000.0);
        request.setIsCold(true);

        MenuItem added = menuService.addMenuItem(MenuType.DRINK, request);
        MenuItem result = menuService.getMenuItemById(added.getId());

        assertEquals(added.getId(), result.getId());
    }

    @Test
    void testGetMenuItemByInvalidIdShouldReturnNull() {
        UUID fakeId = UUID.randomUUID();
        MenuItem result = menuService.getMenuItemById(fakeId);
        assertNull(result);
    }

    @Test
    void testUpdateMenuItem() {
        // First create an item
        MenuItemRequest createRequest = new MenuItemRequest();
        createRequest.setName("Original Item");
        createRequest.setDescription("Original description");
        createRequest.setPrice(10000.0);
        createRequest.setIsSpicy(true);
        
        MenuItem createdItem = menuService.addMenuItem(MenuType.FOOD, createRequest);
        UUID itemId = createdItem.getId();
        
        // Create update request with new values
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        updateRequest.setIsSpicy(false);
        
        // Update the item
        MenuItem updatedItem = menuService.updateMenuItem(itemId, updateRequest);
        
        // Verify the item was updated
        assertNotNull(updatedItem);
        assertEquals(itemId, updatedItem.getId()); // ID should remain the same
        assertEquals("Updated Item", updatedItem.getName());
        assertEquals("Updated description", updatedItem.getDescription());
        assertEquals(15000.0, updatedItem.getPrice());
        assertEquals(false, ((Food)updatedItem).getIsSpicy());
        assertTrue(updatedItem.getAvailable()); // Availability should not change
    }

    @Test
    void testUpdateNonExistentMenuItemReturnsNull() {
        UUID nonExistentId = UUID.randomUUID();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        
        MenuItem result = menuService.updateMenuItem(nonExistentId, updateRequest);
        
        assertNull(result);
    }
}
