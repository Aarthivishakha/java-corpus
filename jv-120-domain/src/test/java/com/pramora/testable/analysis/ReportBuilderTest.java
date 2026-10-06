package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import org.junit.Test;

/** Covers the class that uses a module import declaration. */
public class ReportBuilderTest {

    private final ReportBuilder builder = new ReportBuilder();

    private Order order() {
        var o = new Order("O-R", new Customer("C-R", "R", LoyaltyTier.GOLD, 9));
        o.addLine(new OrderLine("SKU-B", 1, 500L));
        o.addLine(new OrderLine("SKU-A", 2, 100L));
        return o;
    }

    @Test
    public void summariseUsesModuleImportedTypes() {
        assertEquals("O-R, 2 lines, 700c, top=SKU-B", builder.summarise(order()));
    }

    @Test
    public void nullOrderIsHandled() {
        assertEquals("no order", builder.summarise(null));
    }

    @Test
    public void skusAreSortedAndDistinct() {
        assertEquals("[SKU-A, SKU-B]", builder.skus(order()).toString());
    }
}
