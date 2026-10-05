package com.pramora.testable.pricing;

import com.pramora.testable.model.*;
import com.pramora.testable.service.DiscountPolicy;
import com.pramora.testable.service.PricingService;

/** Pricing service entry point for the microservices layout. */
public final class PricingApp {

    private PricingApp() {
    }

    public static void main(String[] args) {
        Order order = new Order("O-PRICING", new Customer("C-1", "Svc", LoyaltyTier.GOLD, 9));
        order.addLine(new OrderLine("SKU-1", 2, 2500L));
        System.out.println(new PricingService(new DiscountPolicy()).priceCents(order));
    }
}
