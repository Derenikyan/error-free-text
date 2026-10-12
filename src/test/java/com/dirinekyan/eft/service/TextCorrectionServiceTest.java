package com.dirinekyan.eft.service;

import com.dirinekyan.eft.domain.Language;
import com.dirinekyan.eft.speller.SpellError;
import com.dirinekyan.eft.speller.SpellerClient;
import com.dirinekyan.eft.speller.SpellerException;
import com.dirinekyan.eft.speller.SpellerOption;
import com.dirinekyan.eft.speller.SpellerProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TextCorrectionServiceTest {

    private final SpellerClient client = mock(SpellerClient.class);

    private TextCorrectionService service(int maxLength) {
        return new TextCorrectionService(client,
                new SpellerProperties("http://localhost", maxLength, Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Test
    void correctsTextWithSuggestions() {
        when(client.check("превет мир", Language.RU, 0))
                .thenReturn(List.of(new SpellError(1, 0, 6, "превет", List.of("привет"))));

        assertEquals("привет мир", service(10_000).correct("превет мир", Language.RU));
    }

    @Test
    void longTextIsSentInPartsAndJoinedBack() {
        when(client.check(anyString(), eq(Language.EN), anyInt())).thenReturn(List.of());
        String text = "word ".repeat(10);

        String result = service(12).correct(text, Language.EN);

        assertEquals(text, result);
        verify(client, times(5)).check(anyString(), eq(Language.EN), anyInt());
    }

    @Test
    void passesIgnoreDigitsOptionWhenTextHasDigits() {
        when(client.check(anyString(), eq(Language.RU), anyInt())).thenReturn(List.of());

        service(10_000).correct("Встреча в 10 часов", Language.RU);

        verify(client).check("Встреча в 10 часов", Language.RU, SpellerOption.IGNORE_DIGITS.flag());
    }

    @Test
    void propagatesSpellerErrors() {
        when(client.check(anyString(), eq(Language.RU), anyInt())).thenThrow(new SpellerException("API down"));

        assertThrows(SpellerException.class, () -> service(10_000).correct("текст", Language.RU));
    }
}
