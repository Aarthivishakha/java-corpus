package com.pramora.testable.app;

import com.pramora.testable.analysis.RiskScorer;
import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.service.DiscountPolicy;
import com.pramora.testable.service.PricingService;
import com.pramora.testable.util.InputSanitizer;

/** Entry point. Prices one order supplied on the command line. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        String reference = args.length > 0 ? InputSanitizer.sanitize(args[0]) : "O-DEMO";
        Order order = new Order(reference, new Customer("C-1", "Demo", LoyaltyTier.GOLD, 12));
        order.addLine(new OrderLine("SKU-1", 2, 1999L));
        order.addLine(new OrderLine("SKU-2", 1, 4999L));

        PricingService pricing = new PricingService(new DiscountPolicy());
        RiskScorer scorer = new RiskScorer();

        System.out.println("order      : " + order.getId());
        System.out.println("placed     : " + order.getPlacedOn());
        System.out.println("lines      : " + order.lineCount());
        System.out.println("subtotal   : " + order.subtotalCents());
        System.out.println("largest    : " + order.largestLine().map(OrderLine::sku).orElse("-"));
        System.out.println("total      : " + pricing.priceCents(order));
        System.out.println("risk score : " + scorer.score(order));
        System.out.println("review     : " + scorer.requiresReview(order));
    }
}
