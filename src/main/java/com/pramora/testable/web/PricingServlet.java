package com.pramora.testable.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.service.DiscountPolicy;
import com.pramora.testable.service.PricingService;
import com.pramora.testable.util.InputSanitizer;

/** Prices an order supplied as a request parameter. */
public class PricingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PricingService pricing = new PricingService(new DiscountPolicy());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reference = InputSanitizer.sanitize(request.getParameter("ref"));
        if (reference.isEmpty()) {
            reference = "O-WEB";
        }
        Order order = new Order(reference, new Customer("C-1", "Web", LoyaltyTier.SILVER, 4));
        order.addLine(new OrderLine("SKU-1", 3, 1250L));

        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            out.println("order=" + order.getId());
            out.println("total=" + pricing.priceCents(order));
        }
    }
}
