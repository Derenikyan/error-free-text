package com.dirinekyan.eft.speller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpellerOptionTest {

    @Test
    void plainTextHasNoOptions() {
        assertEquals(0, SpellerOption.optionsFor("Привет, как дела?"));
    }

    @Test
    void digitsEnableIgnoreDigits() {
        assertEquals(SpellerOption.IGNORE_DIGITS.flag(), SpellerOption.optionsFor("В 2026 году"));
    }

    @Test
    void urlsEnableIgnoreUrls() {
        assertEquals(SpellerOption.IGNORE_URLS.flag(), SpellerOption.optionsFor("Сайт https://example.com тут"));
        assertEquals(SpellerOption.IGNORE_URLS.flag(), SpellerOption.optionsFor("Заходите на yandex.ru"));
    }

    @Test
    void digitsAndUrlsTogether() {
        int expected = SpellerOption.IGNORE_DIGITS.flag() | SpellerOption.IGNORE_URLS.flag();
        assertEquals(expected, SpellerOption.optionsFor("Версия 2 на www.site.org"));
    }

    @Test
    void repeatWordsAndCapitalizationAreNeverEnabled() {
        int options = SpellerOption.optionsFor("Тест тест 123 https://a.io ТЕСТ");
        assertEquals(0, options & SpellerOption.FIND_REPEAT_WORDS.flag());
        assertEquals(0, options & SpellerOption.IGNORE_CAPITALIZATION.flag());
    }
}
