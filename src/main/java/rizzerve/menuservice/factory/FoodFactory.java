package rizzerve.menuservice.factory;

import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;

public class FoodFactory implements MenuItemFactory {
    @Override
    public MenuItem createMenuItem(MenuItemRequest request) {
        Food food = new Food();
        food.setName(request.getName());
        food.setDescription(request.getDescription());
        food.setPrice(request.getPrice());
        food.setIsSpicy(request.getIsSpicy());
        food.setAvailable(true);
        return food;
    }
}
