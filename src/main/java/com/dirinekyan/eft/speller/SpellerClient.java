package com.dirinekyan.eft.speller;

import com.dirinekyan.eft.domain.Language;

import java.util.List;

public interface SpellerClient {

    /**
     * Проверяет текст и возвращает найденные ошибки.
     *
     * @throws SpellerException если API недоступно или вернуло ошибку
     */
    List<SpellError> check(String text, Language language, int options);
}
