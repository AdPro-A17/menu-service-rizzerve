package rizzerve.menuservice.factory;

import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.Drink;
import rizzerve.menuservice.model.MenuItem;

import java.util.UUID;

public class DrinkFactory implements MenuItemFactory {
    @Override
    public MenuItem createMenuItem(MenuItemRequest request) {
        Drink drink = new Drink();
        drink.setId(UUID.randomUUID());
        drink.setName(request.getName());
        drink.setDescription(request.getDescription());
        drink.setPrice(request.getPrice());
        drink.setIsCold(request.getIsCold());
        drink.setImage(request.getImage());
        drink.setAvailable(true);
        return drink;
    }

}
