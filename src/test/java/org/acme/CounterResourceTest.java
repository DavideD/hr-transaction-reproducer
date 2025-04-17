package org.acme;

import static io.quarkus.vertx.VertxContextSupport.subscribeAndAwait;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import jakarta.inject.Inject;

import org.junit.jupiter.api.RepeatedTest;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

@QuarkusTest
class CounterResourceTest {

    @Inject
    CounterResource resource;

    @RepeatedTest(1)
    void should_increase_count_sequentially_using_only_hr_session() throws Throwable {
        // given
        final var counter = subscribeAndAwait(() -> resource.createCounter());
        Log.info("Original Counter: " + counter);

        final var functions = new Supplier[] {
                () -> resource.increaseCounterWithHrSessionAndWait(counter.getId()),
                () -> resource.increaseCounterWithHrSession(counter.getId())
        };
        final var counters = new ArrayList<Integer>();

        // when
        Arrays.stream(functions).parallel().forEach(func -> {
            try {
                final var counter2 = (Counter) subscribeAndAwait(func);
                Log.info("Counter after update: " + counter2);
                counters.add(counter2.getCount());
            } catch (final Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        // then
        assertThat(counters).containsExactly(1, 2);
    }
}