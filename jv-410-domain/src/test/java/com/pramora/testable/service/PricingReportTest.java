package com.pramora.testable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pramora.testable.model.PricingSnapshot;

/** Covers the Java 15 text block and String/CharSequence APIs in {@link PricingReport}. */
public class PricingReportTest {

    private PricingSnapshot snapshot() {
        return new PricingSnapshot("O-15", 10000L, 1500L, 9299L, 35);
    }

    @Test
    public void renderFillsEveryPlaceholder() {
        String out = PricingReport.render(snapshot(), 3);
        assertTrue(out.contains("order O-15"));
        assertTrue(out.contains("lines    : 3"));
        assertTrue(out.contains("subtotal : 10000 cents"));
        assertTrue(out.contains("discount : 1500 cents"));
        assertTrue(out.contains("shipping : 799 cents"));
        assertTrue(out.contains("total    : 9299 cents"));
        assertTrue(out.contains("risk     : 35"));
    }

    @Test
    public void textBlockStripsItsOwnIndentation() {
        String[] lines = PricingReport.render(snapshot(), 1).split("\n");
        assertEquals("order O-15", lines[0]);
        assertTrue("body lines keep their relative indent", lines[1].startsWith("  lines"));
    }

    @Test
    public void textBlockEndsWithANewline() {
        assertTrue(PricingReport.render(snapshot(), 1).endsWith("\n"));
    }

    @Test
    public void reportHasSevenLines() {
        assertEquals(7, PricingReport.lineCount(snapshot(), 2));
    }

    @Test
    public void negativeLineCountIsFlooredAtZero() {
        assertTrue(PricingReport.render(snapshot(), -5).contains("lines    : 0"));
    }

    @Test
    public void renderOfNullIsEmpty() {
        assertEquals("", PricingReport.render(null, 1));
        assertEquals(0, PricingReport.lineCount(null, 1));
        assertEquals("", PricingReport.renderSummary(null));
    }

    @Test
    public void summaryHasItsIndentStripped() {
        String summary = PricingReport.renderSummary(snapshot());
        assertFalse("stripIndent should remove the leading run", summary.startsWith(" "));
        assertTrue(summary.startsWith("O-15 -> 9299 cents"));
        assertTrue(summary.contains("(risk 35)"));
    }

    @Test
    public void captionTranslatesLiteralEscapes() {
        String out = PricingReport.caption("first\\nsecond\\ttabbed");
        assertTrue("backslash-n should become a real newline", out.contains("\n"));
        assertTrue("backslash-t should become a real tab", out.contains("\t"));
        assertFalse("no literal backslash should survive", out.contains("\\n"));
    }

    @Test
    public void captionPrefixCarriesTheEscapedSpace() {
        assertTrue(PricingReport.caption("x").startsWith("note: "));
    }

    @Test
    public void captionOfBlankInputIsEmpty() {
        assertEquals("", PricingReport.caption(null));
        assertEquals("", PricingReport.caption(""));
        assertEquals("", PricingReport.caption("   \t "));
    }

    @Test
    public void isBlankAcceptsAnyCharSequence() {
        assertTrue(PricingReport.isBlank(null));
        assertTrue(PricingReport.isBlank(""));
        assertTrue(PricingReport.isBlank(new StringBuilder()));
        assertTrue(PricingReport.isBlank(new StringBuilder("   ")));
        assertFalse(PricingReport.isBlank(new StringBuilder("x")));
        assertFalse(PricingReport.isBlank("  a  "));
    }
}
