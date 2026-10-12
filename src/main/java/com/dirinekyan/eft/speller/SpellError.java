package com.dirinekyan.eft.speller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Ошибка, найденная Яндекс.Спеллером: позиция и длина слова в тексте и варианты исправления.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpellError(int code, int pos, int len, String word, List<String> s) {
}
