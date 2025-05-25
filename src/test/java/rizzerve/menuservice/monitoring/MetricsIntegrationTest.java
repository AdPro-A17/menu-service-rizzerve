package rizzerve.menuservice.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricsIntegrationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldRegisterCustomMetrics() {
        assertThat(meterRegistry.find("menu.items.created").counter()).isNotNull();
        assertThat(meterRegistry.find("menu.items.creation.time").timer()).isNotNull();
    }
}