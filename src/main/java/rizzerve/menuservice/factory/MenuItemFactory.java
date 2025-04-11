package rizzerve.menuservice.factory;

import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.MenuItem;

public interface MenuItemFactory {
    MenuItem createMenuItem(MenuItemRequest request);
}
