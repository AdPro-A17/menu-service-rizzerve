package rizzerve.menuservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter menuItemCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("menu.items.created")
                .description("Number of menu items created")
                .register(meterRegistry);
    }

    @Bean
    public Timer menuItemCreationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("menu.items.creation.time")
                .description("Time taken to create menu items")
                .register(meterRegistry);
    }

    @Bean
    public Counter menuItemFactoryUsageCounter(MeterRegistry meterRegistry) {
        return Counter.builder("menu.factory.usage")
                .description("Usage of MenuItemFactory")
                .register(meterRegistry);
    }
}