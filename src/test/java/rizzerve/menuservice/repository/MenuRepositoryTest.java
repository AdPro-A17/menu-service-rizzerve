package rizzerve.menuservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.factory.MenuItemFactory;
import rizzerve.menuservice.factory.MenuItemFactoryCreator;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.dto.MenuItemRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MenuRepositoryTest {

    private MenuRepository menuRepository;

    @BeforeEach
    void setUp() {
        this.menuRepository = new MenuRepository();
    }

    @Test
    void testSaveMenuItem() {
        MenuItem item = buildSampleItem("Nasi Goreng");
        MenuItem saved = menuRepository.save(item);

        assertEquals(item.getId(), saved.getId());
    }

    @Test
    void testFindAllReturnsSavedItems() {
        MenuItem item1 = buildSampleItem("Ayam Bakar");
        MenuItem item2 = buildSampleItem("Es Teh");

        menuRepository.save(item1);
        menuRepository.save(item2);

        List<MenuItem> all = menuRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindByIdReturnsCorrectItem() {
        MenuItem item = buildSampleItem("Bakso");
        menuRepository.save(item);

        MenuItem result = menuRepository.findById(item.getId());
        assertNotNull(result);
        assertEquals("Bakso", result.getName());
    }

    @Test
    void testFindByIdReturnsNullForUnknownId() {
        MenuItem result = menuRepository.findById(UUID.randomUUID());
        assertNull(result);
    }

    @Test
    void testDeleteRemovesItemById() {
        MenuItem item = buildSampleItem("Sate Padang");
        menuRepository.save(item);

        MenuItem removed = menuRepository.delete(item.getId());
        assertEquals(item.getId(), removed.getId());

        assertNull(menuRepository.findById(item.getId()));
    }

    private MenuItem buildSampleItem(String name) {
        MenuItemRequest request = new MenuItemRequest();
        request.setName(name);
        request.setDescription("Test Description");
        request.setPrice(20000.0);
        request.setIsSpicy(true);

        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(MenuType.FOOD);
        return factory.createMenuItem(request);
    }
}
