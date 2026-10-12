package com.dirinekyan.eft.speller;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorrectionApplierTest {

    @Test
    void replacesWordsWithFirstSuggestion() {
        String text = "превет как дила";
        List<SpellError> errors = List.of(
                new SpellError(1, 0, 6, "превет", List.of("привет", "привит")),
                new SpellError(1, 11, 4, "дила", List.of("дела")));

        assertEquals("привет как дела", CorrectionApplier.apply(text, errors));
    }

    @Test
    void handlesSuggestionsOfDifferentLength() {
        String text = "helo wrld";
        List<SpellError> errors = List.of(
                new SpellError(1, 0, 4, "helo", List.of("hello")),
                new SpellError(1, 5, 4, "wrld", List.of("world")));

        assertEquals("hello world", CorrectionApplier.apply(text, errors));
    }

    @Test
    void skipsErrorsWithoutSuggestions() {
        String text = "абырвалг";
        List<SpellError> errors = List.of(new SpellError(1, 0, 8, "абырвалг", List.of()));

        assertEquals(text, CorrectionApplier.apply(text, errors));
    }

    @Test
    void skipsErrorsWithMismatchedPosition() {
        String text = "abc";
        List<SpellError> errors = List.of(new SpellError(1, 1, 5, "bcdef", List.of("x")));

        assertEquals(text, CorrectionApplier.apply(text, errors));
    }

    @Test
    void emptyErrorsReturnSameText() {
        assertEquals("ok", CorrectionApplier.apply("ok", List.of()));
        assertEquals("ok", CorrectionApplier.apply("ok", null));
    }
}
