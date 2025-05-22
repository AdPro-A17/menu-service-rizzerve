package rizzerve.menuservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Drink;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.repository.MenuRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        this.menuService = new MenuService(menuRepository);
    }

    @Test
    void testAddNewMenuItem() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Mie Goreng");
        request.setDescription("Fried noodle");
        request.setPrice(25000.0);
        request.setIsSpicy(true);
        request.setImage("https://example.com/mie-goreng.jpg");

        // Mock repository behavior
        Mockito.when(menuRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem item = invocation.getArgument(0);
            return item;
        });

        MenuItem savedItem = menuService.addMenuItem(MenuType.FOOD, request);

        assertNotNull(savedItem.getId());
        assertEquals("Mie Goreng", savedItem.getName());
        assertEquals(25000.0, savedItem.getPrice());
        assertEquals("https://example.com/mie-goreng.jpg", savedItem.getImage());
        assertInstanceOf(Food.class, savedItem);
    }

    @Test
    void testAddMenuItemWithEmptyNameShouldThrow() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("");
        request.setDescription("Test description");
        request.setPrice(10000.0);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            menuService.addMenuItem(MenuType.FOOD, request);
        });
        
        assertTrue(exception.getMessage().contains("Name"));
    }

    @Test
    void testGetAllMenuItems() {
        Food sampleFood = new Food();
        sampleFood.setId(UUID.randomUUID());
        sampleFood.setName("Nasi Goreng");
        sampleFood.setDescription("Fried rice");
        sampleFood.setPrice(27000.0);
        sampleFood.setIsSpicy(true);
        sampleFood.setImage("https://example.com/nasi-goreng.jpg");
        sampleFood.setAvailable(true);

        when(menuRepository.findAll()).thenReturn(List.of(sampleFood));

        List<MenuItem> items = menuService.getAllMenuItems();
        assertEquals(1, items.size());
        assertEquals("https://example.com/nasi-goreng.jpg", items.get(0).getImage());
    }

    @Test
    void testGetMenuItemById() {
        UUID id = UUID.randomUUID();
        Food sampleFood = new Food();
        sampleFood.setId(id);
        sampleFood.setName("Teh Manis");
        sampleFood.setDescription("Sweet tea");
        sampleFood.setPrice(7000.0);
        sampleFood.setIsSpicy(false);
        sampleFood.setAvailable(true);

        when(menuRepository.findById(id)).thenReturn(Optional.of(sampleFood));

        MenuItem result = menuService.getMenuItemById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetMenuItemByInvalidIdShouldReturnNull() {
        UUID fakeId = UUID.randomUUID();
        when(menuRepository.findById(fakeId)).thenReturn(Optional.empty());
        
        MenuItem result = menuService.getMenuItemById(fakeId);
        assertNull(result);
    }

    @Test
    void testUpdateMenuItem() {
        UUID itemId = UUID.randomUUID();
        
        // Create sample food item
        Food existingFood = new Food();
        existingFood.setId(itemId);
        existingFood.setName("Original Item");
        existingFood.setDescription("Original description");
        existingFood.setPrice(10000.0);
        existingFood.setIsSpicy(true);
        existingFood.setImage("https://example.com/original.jpg");
        existingFood.setAvailable(true);
        
        // Create updated request
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        updateRequest.setIsSpicy(false);
        updateRequest.setImage("https://example.com/updated.jpg");
        
        // Mock repository behavior
        when(menuRepository.findById(itemId)).thenReturn(Optional.of(existingFood));
        when(menuRepository.save(any(MenuItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        MenuItem updatedItem = menuService.updateMenuItem(itemId, updateRequest);
        
        assertNotNull(updatedItem);
        assertEquals(itemId, updatedItem.getId());
        assertEquals("Updated Item", updatedItem.getName());
        assertEquals("Updated description", updatedItem.getDescription());
        assertEquals(15000.0, updatedItem.getPrice());
        assertEquals("https://example.com/updated.jpg", updatedItem.getImage());
        assertEquals(false, ((Food)updatedItem).getIsSpicy());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void testUpdateNonExistentMenuItemReturnsNull() {
        UUID nonExistentId = UUID.randomUUID();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        
        when(menuRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        MenuItem result = menuService.updateMenuItem(nonExistentId, updateRequest);
        
        assertNull(result);
    }

    @Test
    void testDeleteMenuItem() {
        UUID itemId = UUID.randomUUID();
        
        Food foodItem = new Food();
        foodItem.setId(itemId);
        foodItem.setName("Deletable Item");
        foodItem.setDescription("Will be deleted");
        foodItem.setPrice(10000.0);
        foodItem.setIsSpicy(true);
        foodItem.setAvailable(true);
        
        when(menuRepository.findById(itemId)).thenReturn(Optional.of(foodItem));
        
        MenuItem deletedItem = menuService.deleteMenuItem(itemId);
        
        assertNotNull(deletedItem);
        assertEquals(itemId, deletedItem.getId());
        assertEquals("Deletable Item", deletedItem.getName());
    }

    @Test
    void testDeleteNonExistentMenuItemReturnsNull() {
        UUID nonExistentId = UUID.randomUUID();
        when(menuRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        MenuItem result = menuService.deleteMenuItem(nonExistentId);
        assertNull(result);
    }
    
    @Test
    void testAddMenuItemWithDrinkType() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Orange Juice");
        request.setDescription("Fresh orange juice");
        request.setPrice(15000.0);
        request.setIsCold(true);
        request.setImage("https://example.com/orange-juice.jpg");

        // Mock the save method to return a drink
        Mockito.when(menuRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem item = invocation.getArgument(0);
            if (item instanceof Drink) {
                // Ensure the properties are correctly set
                Drink drink = (Drink) item;
                drink.setName(request.getName());
                drink.setDescription(request.getDescription());
                drink.setPrice(request.getPrice());
                drink.setIsCold(request.getIsCold());
                drink.setImage(request.getImage());
                drink.setAvailable(true);
            }
            return item;
        });

        MenuItem savedItem = menuService.addMenuItem(MenuType.DRINK, request);

        assertNotNull(savedItem.getId());
        assertEquals("Orange Juice", savedItem.getName());
        assertEquals(15000.0, savedItem.getPrice());
        assertEquals("https://example.com/orange-juice.jpg", savedItem.getImage());
        assertInstanceOf(Drink.class, savedItem);
        assertTrue(((Drink)savedItem).getIsCold());
    }
}
