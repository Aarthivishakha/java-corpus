package com.pramora.testable.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An order placed by a customer. Java 10 source level: local `var`, `List.copyOf` and
 * `Collectors.toUnmodifiableList`, none of which exists in Java 9.
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
        return List.copyOf(lines);
    }

    /** Collectors.toUnmodifiableList, added in Java 10. */
    public List<String> skus() {
        return lines.stream().map(OrderLine::getSku)
                .collect(java.util.stream.Collectors.toUnmodifiableList());
    }

    /** Optional.orElseThrow with no argument, added in Java 10. */
    public OrderLine requireLargestLine() {
        return largestLine().orElseThrow();
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

    /** Optional.or, added in Java 9: falls back without unwrapping. */
    public Optional<OrderLine> largestLineOrFirst() {
        return largestLine().or(() -> lines.isEmpty() ? Optional.empty() : Optional.of(lines.get(0)));
    }

    /** Optional.stream, added in Java 9. */
    public List<String> largestSku() {
        return largestLine().stream().map(OrderLine::getSku).collect(java.util.stream.Collectors.toList());
    }

    /** Stream.takeWhile, added in Java 9: lines up to the first zero-priced one. */
    public List<OrderLine> linesUntilFree() {
        return lines.stream()
                .takeWhile(line -> line.getUnitPriceCents() > 0L)
                .collect(java.util.stream.Collectors.toList());
    }
}
