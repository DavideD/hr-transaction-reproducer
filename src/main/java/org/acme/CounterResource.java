package org.acme;

import static io.smallrye.mutiny.vertx.MutinyHelper.executor;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

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
        final var originalContext = Vertx.currentContext();

        return repository.findById(id, LockModeType.PESSIMISTIC_WRITE) //
                .onItem().invoke(() -> Log.info("increaseCounterAndWait() acquired lock!")) //
                .onItem().invoke(Counter::increase) //
                .onItem().delayIt().by(Duration.ofSeconds(1)) //
                .emitOn(executor(originalContext));
    }

    public Uni<Counter> increaseCounterWithHrSession(final Long id) {
        return sessionFactory.withSession(session -> session.find(Counter.class, id, LockModeType.PESSIMISTIC_WRITE)) //
                .onItem().invoke(() -> Log.info("increaseCounterWithSession() acquired lock!")) //
                .onItem().invoke(Counter::increase);
    }

    public Uni<Counter> increaseCounterWithPanache(final Long id) {
        return repository.findById(id, LockModeType.PESSIMISTIC_WRITE) //
                .onItem().invoke(() -> Log.info("increaseCounterWithPanache() acquired lock!")) //
                .onItem().invoke(Counter::increase);
    }
}
