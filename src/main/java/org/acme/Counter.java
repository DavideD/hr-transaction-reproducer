package org.acme;

import java.util.StringJoiner;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Counter {

    @Id
    @GeneratedValue
    private Long id;
    private int count;

    public Counter() {
    }

    public Counter(final int count) {
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Counter.class.getSimpleName() + "[", "]").add("id=" + id).add("counter=" + count)
                .toString();
    }

    public void increase() {
        this.count++;
    }
}
