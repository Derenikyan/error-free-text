package com.dirinekyan.eft.speller;

import java.util.Comparator;
import java.util.List;

/**
 * Применяет найденные Спеллером исправления к тексту: каждое ошибочное слово заменяется
 * первым предложенным вариантом. Замены идут с конца текста, чтобы не сбивать позиции.
 */
public final class CorrectionApplier {

    private CorrectionApplier() {
    }

    public static String apply(String text, List<SpellError> errors) {
        if (errors == null || errors.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text);
        errors.stream()
                .filter(e -> e.s() != null && !e.s().isEmpty())
                .filter(e -> e.pos() >= 0 && e.len() > 0 && e.pos() + e.len() <= text.length())
                .filter(e -> e.word() == null || text.regionMatches(e.pos(), e.word(), 0, e.len()))
                .sorted(Comparator.comparingInt(SpellError::pos).reversed())
                .forEach(e -> result.replace(e.pos(), e.pos() + e.len(), e.s().get(0)));
        return result.toString();
    }
}
