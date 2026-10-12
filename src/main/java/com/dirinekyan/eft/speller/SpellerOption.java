package com.dirinekyan.eft.speller;

import java.util.regex.Pattern;

/**
 * Опции Яндекс.Спеллера (битовая маска параметра {@code options}).
 */
public enum SpellerOption {
    IGNORE_DIGITS(2),
    IGNORE_URLS(4),
    FIND_REPEAT_WORDS(8),
    IGNORE_CAPITALIZATION(512);

    private static final Pattern DIGITS = Pattern.compile("\\d");
    private static final Pattern URL = Pattern.compile(
            "(?iu)(https?://\\S+|www\\.\\S+|\\b[\\p{L}\\d-]+(\\.[\\p{L}\\d-]+)*\\.(ru|com|org|net|io|рф|su|by|kz|uz|info|dev|me)\\b)");

    private final int flag;

    SpellerOption(int flag) {
        this.flag = flag;
    }

    public int flag() {
        return flag;
    }

    /**
     * Считает маску опций для текста: IGNORE_DIGITS — если есть цифры, IGNORE_URLS — если есть URL.
     * FIND_REPEAT_WORDS и IGNORE_CAPITALIZATION по условию задачи всегда выключены.
     */
    public static int optionsFor(String text) {
        int options = 0;
        if (DIGITS.matcher(text).find()) {
            options |= IGNORE_DIGITS.flag;
        }
        if (URL.matcher(text).find()) {
            options |= IGNORE_URLS.flag;
        }
        return options;
    }
}
