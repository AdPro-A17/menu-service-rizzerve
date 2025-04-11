package rizzerve.menuservice.factory;

import org.junit.jupiter.api.Test;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Drink;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;

import static org.junit.jupiter.api.Assertions.*;

public class MenuItemFactoryTest {

    @Test
    void testCreateFoodItem() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Spicy Chicken");
        request.setDescription("Hot grilled chicken");
        request.setPrice(35000.0);
        request.setIsSpicy(true); // <-- new boolean field

        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(MenuType.FOOD);
        MenuItem item = factory.createMenuItem(request);

        assertInstanceOf(Food);
        Food food = (Food) item;

        assertEquals("Spicy Chicken", food.getName());
        assertEquals("Hot grilled chicken", food.getDescription());
        assertEquals(35000.0, food.getPrice());
        assertTrue(food.getIsSpicy());
        assertTrue(food.getAvailable());
    }

    @Test
    void testCreateDrinkItem() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Iced Lemon Tea");
        request.setDescription("Cold and refreshing");
        request.setPrice(18000.0);
        request.setIsCold(true); // boolean for drinks

        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(MenuType.DRINK);
        MenuItem item = factory.createMenuItem(request);

        assertInstanceOf(Drink);
        Drink drink = (Drink) item;

        assertEquals("Iced Lemon Tea", drink.getName());
        assertEquals("Cold and refreshing", drink.getDescription());
        assertEquals(18000.0, drink.getPrice());
        assertTrue(drink.getIsCold());
        assertTrue(drink.getAvailable());
    }

    @Test
    void testInvalidFactoryShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            MenuItemFactoryCreator.getFactory(null);
        });
    }
}
