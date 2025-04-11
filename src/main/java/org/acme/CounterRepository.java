package org.acme;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;

@ApplicationScoped
public class CounterRepository implements PanacheRepositoryBase<Counter, Long> {
}
