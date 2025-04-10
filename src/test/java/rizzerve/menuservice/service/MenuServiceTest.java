package rizzerve.menuservice.service;

import org.junit.jupiter.api.Test;
import rizzerve.menuservice.model.Menu;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private MenuService menuService; // not initialized yet!

    @Test
    void testCreateMenu() {
        Menu newMenu = new Menu();
        newMenu.setName("Nasi Goreng");
        newMenu.setDescription("Nasi dengan bumbu khas");
        newMenu.setPrice(25000);

        Menu createdMenu = menuService.createMenu(newMenu);

        assertNotNull(createdMenu.getId()); // Expecting auto-generated ID
        assertEquals("Nasi Goreng", createdMenu.getName());
        assertEquals("Nasi dengan bumbu khas", createdMenu.getDescription());
        assertEquals(25000, createdMenu.getPrice());
    }
}
