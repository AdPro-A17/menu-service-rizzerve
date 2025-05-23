package rizzerve.menuservice.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.factory.MenuItemFactory;
import rizzerve.menuservice.factory.MenuItemFactoryCreator;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.dto.MenuItemRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class MenuRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MenuRepository menuRepository;

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

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        assertEquals(2, menuRepository.findAll().size());
    }

    @Test
    void testFindByIdReturnsCorrectItem() {
        MenuItem item = buildSampleItem("Bakso");
        entityManager.persist(item);
        entityManager.flush();

        Optional<MenuItem> result = menuRepository.findById(item.getId());
        assertTrue(result.isPresent());
        assertEquals("Bakso", result.get().getName());
    }

    @Test
    void testFindByIdReturnsEmptyForUnknownId() {
        Optional<MenuItem> result = menuRepository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    private MenuItem buildSampleItem(String name) {
        MenuItemRequest request = new MenuItemRequest();
        request.setName(name);
        request.setDescription("Test Description");
        request.setPrice(20000.0);
        request.setIsSpicy(true);
        request.setImage("https://example.com/" + name.toLowerCase().replace(" ", "-") + ".jpg");

        MenuItemFactory factory = MenuItemFactoryCreator.getFactory(MenuType.FOOD);
        return factory.createMenuItem(request);
    }

    @Test
    void testSaveAndRetrieveWithImageField() {
        // Create item with image URL
        Food foodItem = new Food();
        foodItem.setId(UUID.randomUUID());
        foodItem.setName("Pizza");
        foodItem.setDescription("Delicious pizza");
        foodItem.setPrice(25000.0);
        foodItem.setIsSpicy(false);
        foodItem.setAvailable(true);
        foodItem.setImage("https://example.com/pizza.jpg");

        // Save to DB
        MenuItem savedItem = menuRepository.save(foodItem);

        // Retrieve and verify
        MenuItem retrievedItem = menuRepository.findById(savedItem.getId()).orElse(null);
        assertNotNull(retrievedItem);
        assertEquals("https://example.com/pizza.jpg", retrievedItem.getImage());
    }
}
