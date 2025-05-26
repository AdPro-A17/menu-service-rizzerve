package rizzerve.menuservice.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    private final MeterRegistry meterRegistry;

    public PerformanceMonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(Timed) || within(rizzerve.menuservice.service.*)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Timer timer = Timer.builder("method.execution.time")
                .tag("method", methodName)
                .register(meterRegistry);

        return timer.recordCallable(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new PerformanceMonitoringException("Error during performance monitoring of method: " + methodName, throwable);
            }
        });
    }

    public static class PerformanceMonitoringException extends RuntimeException {
        public PerformanceMonitoringException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}