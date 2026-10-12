package com.dirinekyan.eft.speller;

import java.util.ArrayList;
import java.util.List;

/**
 * Делит длинный текст на части не длиннее заданного лимита.
 * Режет по последнему пробельному символу внутри окна, чтобы не разрывать слова;
 * склейка всех частей в исходном порядке даёт исходный текст без изменений.
 */
public final class TextSplitter {

    private TextSplitter() {
    }

    public static List<String> split(String text, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (text.length() - start > maxLength) {
            int end = start + maxLength;
            int cut = lastWhitespace(text, start, end);
            if (cut <= start) {
                cut = end;
            }
            parts.add(text.substring(start, cut));
            start = cut;
        }
        parts.add(text.substring(start));
        return parts;
    }

    private static int lastWhitespace(String text, int from, int to) {
        for (int i = to; i > from; i--) {
            if (Character.isWhitespace(text.charAt(i - 1))) {
                return i;
            }
        }
        return -1;
    }
}
