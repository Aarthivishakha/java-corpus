package com.pramora.testable.catalog;

import com.pramora.testable.model.OrderLine;
import com.pramora.testable.util.InputSanitizer;

/** Catalog service entry point for the microservices layout. */
public final class CatalogApp {

    private CatalogApp() {
    }

    public static void main(String[] args) {
        String sku = args.length > 0 ? InputSanitizer.sanitize(args[0]) : "SKU-1";
        OrderLine line = new OrderLine(sku, 1, 1000L);
        System.out.println(line.getSku() + " " + line.extendedCents());
    }
}
