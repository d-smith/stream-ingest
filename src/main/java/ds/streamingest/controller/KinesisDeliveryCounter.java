package ds.streamingest.controller;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Component
@Endpoint(id = "kinesis")
public class KinesisDeliveryCounter {
    private AtomicLong attempts = new AtomicLong(0);
    private AtomicLong successes = new AtomicLong(0);
    private AtomicLong failures = new AtomicLong(0);

    @ReadOperation
    public Map<String,Long> counts() {
        ConcurrentHashMap<String,Long> counts = new ConcurrentHashMap<>();

        counts.put("attempts", attempts.getPlain());
        counts.put("successes", successes.getPlain());
        counts.put("failures", failures.getPlain());

        return counts;
    }

    public void incrementAttempts() {
        attempts.incrementAndGet();
    }

    public void incrementFailures() {
        failures.incrementAndGet();
    }

    public void incrementSuccesses() {
        successes.incrementAndGet();
    }
}
