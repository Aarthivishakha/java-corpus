package com.pramora.testable.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An order placed by a customer. Java 21 source level: the sequenced-collection
 * methods getFirst, getLast and reversed, none of which exists on List before 21.
 */
public class Order {

    private final String id;
    private final Customer customer;
    private final List<OrderLine> lines = new ArrayList<>();
    private final LocalDate placedOn;
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(String id, Customer customer) {
        this(id, customer, LocalDate.of(2026, 1, 15));
    }

    public Order(String id, Customer customer, LocalDate placedOn) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("order id is required");
        }
        if (customer == null) {
            throw new IllegalArgumentException("customer is required");
        }
        this.id = id;
        this.customer = customer;
        this.placedOn = placedOn == null ? LocalDate.of(2026, 1, 15) : placedOn;
    }

    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDate getPlacedOn() {
        return placedOn;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        this.status = status;
    }

    public void addLine(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("line is required");
        }
        lines.add(line);
    }

    public List<OrderLine> getLines() {
        return lines.stream().toList();
    }

    public boolean isEmpty() {
        return largestLine().isEmpty();
    }

    public int lineCount() {
        return lines.size();
    }

    public long subtotalCents() {
        var total = lines.stream().mapToLong(OrderLine::extendedCents).sum();
        return total;
    }

    public Optional<OrderLine> largestLine() {
        return lines.stream().max(java.util.Comparator.comparingLong(OrderLine::extendedCents));
    }

    /** SequencedCollection.getFirst, added to List in Java 21. */
    public Optional<OrderLine> firstLine() {
        return lines.isEmpty() ? Optional.empty() : Optional.of(lines.getFirst());
    }

    /** SequencedCollection.getLast, added to List in Java 21. */
    public Optional<OrderLine> lastLine() {
        return lines.isEmpty() ? Optional.empty() : Optional.of(lines.getLast());
    }

    /** SequencedCollection.reversed, added to List in Java 21. */
    public List<OrderLine> linesReversed() {
        return List.copyOf(lines.reversed());
    }
}
