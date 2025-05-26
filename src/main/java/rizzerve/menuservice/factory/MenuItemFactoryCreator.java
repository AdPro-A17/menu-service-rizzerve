package rizzerve.menuservice.factory;

import rizzerve.menuservice.enums.MenuType;

public class MenuItemFactoryCreator {
    public static MenuItemFactory getFactory(MenuType type) {
        if (type == null) {
            throw new IllegalArgumentException("Menu type cannot be null");
        }

        return switch (type) {
            case FOOD -> new FoodFactory();
            case DRINK -> new DrinkFactory();
        };
    }
}
