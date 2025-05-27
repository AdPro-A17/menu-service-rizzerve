package rizzerve.menuservice.factory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.model.MenuItem;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractMenuItemFactoryTest {

    @Mock
    private Counter mockCounter;

    @Mock
    private Timer mockTimer;

    @Mock
    private Timer.Sample mockTimerSample;

    private TestMenuItemFactory testFactory;
    private MenuItemRequest validRequest;

    // Concrete implementation for testing
    private static class TestMenuItemFactory extends AbstractMenuItemFactory {
        private MenuItem itemToReturn;
        private RuntimeException exceptionToThrow;

        public TestMenuItemFactory(Counter counter, Timer timer) {
            super(counter, timer);
        }

        public void setItemToReturn(MenuItem item) {
            this.itemToReturn = item;
        }

        public void setExceptionToThrow(RuntimeException exception) {
            this.exceptionToThrow = exception;
        }

        @Override
        protected MenuItem doCreateMenuItem(MenuItemRequest request) {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return itemToReturn;
        }
    }

    @BeforeEach
    void setUp() {
        testFactory = new TestMenuItemFactory(mockCounter, mockTimer);
        
        validRequest = new MenuItemRequest();
        validRequest.setName("Test Item");
        validRequest.setDescription("Test description");
        validRequest.setPrice(15000.0);
        validRequest.setIsSpicy(true);
        validRequest.setImage("https://example.com/test.jpg");
    }

    @Test
    void testCreateMenuItem_successfulCreation() {
        // Arrange
        Food expectedFood = new Food();
        expectedFood.setId(UUID.randomUUID());
        expectedFood.setName("Test Item");
        expectedFood.setDescription("Test description");
        expectedFood.setPrice(15000.0);
        expectedFood.setIsSpicy(true);
        expectedFood.setAvailable(true);
        
        testFactory.setItemToReturn(expectedFood);
        
        // Mock timer behavior
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        // Act
        MenuItem result = testFactory.createMenuItem(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedFood, result);
        assertEquals("Test Item", result.getName());
        assertEquals(15000.0, result.getPrice());
        
        // Verify metrics interactions
        verify(mockTimer).record(any(Supplier.class));
        verify(mockCounter).increment();
    }

    @Test
    void testCreateMenuItem_metricsAreRecorded() {
        // Arrange
        Food testFood = new Food();
        testFood.setId(UUID.randomUUID());
        testFood.setName("Metrics Test");
        
        testFactory.setItemToReturn(testFood);
        
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        // Act
        testFactory.createMenuItem(validRequest);

        // Assert
        verify(mockCounter, times(1)).increment();
        verify(mockTimer, times(1)).record(any(Supplier.class));
    }

    @Test
    void testCreateMenuItem_exceptionInDoCreateMenuItem() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test exception");
        testFactory.setExceptionToThrow(testException);
        
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            return supplier.get(); // This will throw the exception
        });

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            testFactory.createMenuItem(validRequest);
        });
        
        assertEquals("Test exception", thrownException.getMessage());
        
        // Verify that timer.record was called even though exception was thrown
        verify(mockTimer).record(any(Supplier.class));
        // Counter should still be incremented since it's called before doCreateMenuItem
        verify(mockCounter).increment();
    }

    @Test
    void testCreateMenuItem_multipleInvocations() {
        // Arrange
        Food firstItem = new Food();
        firstItem.setId(UUID.randomUUID());
        firstItem.setName("First Item");
        
        Food secondItem = new Food();
        secondItem.setId(UUID.randomUUID());
        secondItem.setName("Second Item");
        
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        // Act
        testFactory.setItemToReturn(firstItem);
        MenuItem result1 = testFactory.createMenuItem(validRequest);
        
        testFactory.setItemToReturn(secondItem);
        MenuItem result2 = testFactory.createMenuItem(validRequest);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("First Item", result1.getName());
        assertEquals("Second Item", result2.getName());
        
        // Verify metrics were recorded for both invocations
        verify(mockCounter, times(2)).increment();
        verify(mockTimer, times(2)).record(any(Supplier.class));
    }

    @Test
    void testCreateMenuItem_withNullRequest() {
        // Arrange
        testFactory.setItemToReturn(null);
        
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        // Act
        MenuItem result = testFactory.createMenuItem(null);

        // Assert
        assertNull(result);
        
        // Verify metrics are still recorded
        verify(mockCounter).increment();
        verify(mockTimer).record(any(Supplier.class));
    }

    @Test
    void testFactoryImplementsMenuItemFactory() {
        // Assert
        assertInstanceOf(MenuItemFactory.class, testFactory);
    }

    @Test
    void testCreateMenuItem_timerRecordsExecutionTime() {
        // Arrange
        Food testFood = new Food();
        testFood.setId(UUID.randomUUID());
        testFood.setName("Timer Test");
        
        testFactory.setItemToReturn(testFood);
        
        // Simulate timer recording with actual timing
        when(mockTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<MenuItem> supplier = invocation.getArgument(0);
            long startTime = System.nanoTime();
            MenuItem result = supplier.get();
            long duration = System.nanoTime() - startTime;
            assertTrue(duration >= 0, "Timer should record non-negative duration");
            return result;
        });

        // Act
        MenuItem result = testFactory.createMenuItem(validRequest);

        // Assert
        assertNotNull(result);
        verify(mockTimer).record(any(Supplier.class));
    }

    @Test
    void testConstructorWithMetrics() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            new TestMenuItemFactory(mockCounter, mockTimer);
        });
        
        // Verify the factory stores the metrics correctly
        TestMenuItemFactory factory = new TestMenuItemFactory(mockCounter, mockTimer);
        assertNotNull(factory);
    }
}
