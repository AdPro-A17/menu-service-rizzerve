package rizzerve.menuservice.factory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.MenuItem;

public abstract class AbstractMenuItemFactory implements MenuItemFactory {

    @Autowired
    protected Counter menuItemFactoryUsageCounter;

    @Autowired
    protected Timer menuItemCreationTimer;

    @Override
    public MenuItem createMenuItem(MenuItemRequest request) {
        return menuItemCreationTimer.record(() -> {
            menuItemFactoryUsageCounter.increment();
            return doCreateMenuItem(request);
        });
    }

    protected abstract MenuItem doCreateMenuItem(MenuItemRequest request);
}