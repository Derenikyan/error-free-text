package com.dirinekyan.eft.domain;

/**
 * Языки, которые поддерживает сервис. Значение {@link #code} передаётся в Яндекс.Спеллер.
 */
public enum Language {
    RU("ru"),
    EN("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
