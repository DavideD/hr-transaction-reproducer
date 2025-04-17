package org.acme;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.ContextInternal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import java.time.Duration;

@ApplicationScoped
@WithTransaction
public class CounterResource {

    @Inject
    CounterRepository repository;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    public Uni<Counter> createCounter() {
        return repository.persist(new Counter(0));
    }

    public Uni<Counter> increaseCounterWithPanacheAndWait(final Long id) {
        final var context = ContextInternal.current();

        return repository.findById(id, LockModeType.PESSIMISTIC_WRITE)
                .onItem().invoke(counter -> Log.info("increaseCounterAndWait() acquired lock and wait: " + counter))
                .onItem().invoke(Counter::increase)
                .onItem().delayIt().by(Duration.ofSeconds(1))
                .emitOn(context.executor());
    }

    public Uni<Counter> increaseCounterWithHrSessionAndWait(final Long id) {
        final var context = ContextInternal.current();

        return sessionFactory
                .withSession( session -> session.find( Counter.class, id, LockModeType.PESSIMISTIC_WRITE ) )
                .onItem().invoke(counter -> Log.info("increaseCounterWithSession() acquired lock and wait: " + counter))
                .onItem().invoke(Counter::increase)
                .onItem().delayIt().by(Duration.ofSeconds(1))
                .emitOn(context.executor());
    }

    public Uni<Counter> increaseCounterWithHrSession(final Long id) {
        return sessionFactory
                .withSession( session -> session.find( Counter.class, id, LockModeType.PESSIMISTIC_WRITE ) )
                .onItem().invoke(counter -> Log.info("increaseCounterWithSession() acquired lock: " + counter))
                .onItem().invoke(Counter::increase);
    }

    public Uni<Counter> increaseCounterWithPanache(final Long id) {
        return repository
                .findById(id, LockModeType.PESSIMISTIC_WRITE) //
                .onItem().invoke(counter -> Log.info("increaseCounterWithPanache() acquired lock: " + counter))
                .onItem().invoke(Counter::increase);
    }
}
