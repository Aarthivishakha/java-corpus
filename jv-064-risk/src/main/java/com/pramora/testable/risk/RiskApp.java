package com.pramora.testable.risk;

import com.pramora.testable.analysis.RiskScorer;
import com.pramora.testable.model.*;

/** Risk service entry point for the microservices layout. */
public final class RiskApp {

    private RiskApp() {
    }

    public static void main(String[] args) {
        Order order = new Order("O-RISK", new Customer("C-2", "Svc", LoyaltyTier.STANDARD, 0));
        order.addLine(new OrderLine("SKU-9", 80, 90000L));
        System.out.println(new RiskScorer().score(order));
    }
}
