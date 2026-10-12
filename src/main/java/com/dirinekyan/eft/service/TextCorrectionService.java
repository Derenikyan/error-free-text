package com.dirinekyan.eft.service;

import com.dirinekyan.eft.domain.Language;
import com.dirinekyan.eft.speller.CorrectionApplier;
import com.dirinekyan.eft.speller.SpellError;
import com.dirinekyan.eft.speller.SpellerClient;
import com.dirinekyan.eft.speller.SpellerOption;
import com.dirinekyan.eft.speller.SpellerProperties;
import com.dirinekyan.eft.speller.TextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Исправляет текст через Спеллер: делит длинный текст на части, проверяет каждую
 * и собирает исправленный результат обратно.
 */
@Service
public class TextCorrectionService {

    private final SpellerClient spellerClient;
    private final int maxTextLength;

    public TextCorrectionService(SpellerClient spellerClient, SpellerProperties properties) {
        this.spellerClient = spellerClient;
        this.maxTextLength = properties.maxTextLength();
    }

    public String correct(String text, Language language) {
        int options = SpellerOption.optionsFor(text);
        StringBuilder result = new StringBuilder(text.length());
        for (String part : TextSplitter.split(text, maxTextLength)) {
            List<SpellError> errors = spellerClient.check(part, language, options);
            result.append(CorrectionApplier.apply(part, errors));
        }
        return result.toString();
    }
}
