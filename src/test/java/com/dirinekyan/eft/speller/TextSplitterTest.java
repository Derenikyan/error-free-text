package com.dirinekyan.eft.speller;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextSplitterTest {

    @Test
    void shortTextIsNotSplit() {
        assertEquals(List.of("привет мир"), TextSplitter.split("привет мир", 100));
    }

    @Test
    void splitsOnWhitespaceAndKeepsOriginalText() {
        String text = "aaaa bbbb cccc dddd";
        List<String> parts = TextSplitter.split(text, 10);

        parts.forEach(p -> assertTrue(p.length() <= 10, "part too long: " + p));
        assertEquals(text, String.join("", parts));
        assertEquals("aaaa bbbb ", parts.get(0));
    }

    @Test
    void cutsLongWordWithoutSpaces() {
        String text = "a".repeat(25);
        List<String> parts = TextSplitter.split(text, 10);

        assertEquals(List.of("a".repeat(10), "a".repeat(10), "a".repeat(5)), parts);
    }

    @Test
    void textOfTwentyThousandCharsIsSplitIntoChunksUpToTenThousand() {
        String text = "слово ".repeat(4000);
        List<String> parts = TextSplitter.split(text, 10_000);

        assertTrue(parts.size() >= 3);
        parts.forEach(p -> assertTrue(p.length() <= 10_000));
        assertEquals(text, String.join("", parts));
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> TextSplitter.split("abc", 0));
    }
}
