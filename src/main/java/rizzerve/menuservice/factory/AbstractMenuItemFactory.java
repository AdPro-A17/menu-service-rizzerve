package rizzerve.menuservice.factory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.MenuItem;

public abstract class AbstractMenuItemFactory implements MenuItemFactory {

    protected final Counter menuItemFactoryUsageCounter;
    protected final Timer menuItemCreationTimer;

    protected AbstractMenuItemFactory(Counter menuItemFactoryUsageCounter, Timer menuItemCreationTimer) {
        this.menuItemFactoryUsageCounter = menuItemFactoryUsageCounter;
        this.menuItemCreationTimer = menuItemCreationTimer;
    }

    @Override
    public MenuItem createMenuItem(MenuItemRequest request) {
        return menuItemCreationTimer.record(() -> {
            menuItemFactoryUsageCounter.increment();
            return doCreateMenuItem(request);
        });
    }

    protected abstract MenuItem doCreateMenuItem(MenuItemRequest request);
}