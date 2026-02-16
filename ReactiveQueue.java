import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactiveQueueExample {

    public static void main(String[] args) throws InterruptedException {
        // Create a sink that acts like a reactive queue (multicast to many subscribers)
        Sinks.Many<String> queue = Sinks.many().multicast().onBackpressureBuffer();

        // Create a Flux from the sink
        Flux<String> messageStream = queue.asFlux();

        // Subscriber 1: Processes messages with a delay
        messageStream
                .delayElements(Duration.ofMillis(500))
                .subscribe(msg -> System.out.println("[Consumer 1] Received: " + msg));

        // Subscriber 2: Processes messages instantly
        messageStream
                .subscribe(msg -> System.out.println("[Consumer 2] Received: " + msg));

        // Simulate producing messages
        AtomicInteger counter = new AtomicInteger(1);
        Flux.interval(Duration.ofMillis(300))
                .take(10) // produce 10 messages
                .map(i -> "Message-" + counter.getAndIncrement())
                .subscribe(msg -> {
                    Sinks.EmitResult result = queue.tryEmitNext(msg);
                    if (result.isFailure()) {
                        System.err.println("Failed to enqueue: " + msg + " Reason: " + result);
                    }
                });

        // Keep the program alive long enough to process messages
        Thread.sleep(6000);
    }
}