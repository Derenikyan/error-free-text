package com.dirinekyan.eft.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotNull(message = "Текст обязателен")
        @Size(min = 3, message = "Текст должен содержать минимум 3 символа")
        @Pattern(regexp = "(?s).*\\p{L}.*", message = "Текст не может состоять только из цифр и спецсимволов")
        String text,

        @NotBlank(message = "Язык обязателен")
        @Pattern(regexp = "EN|RU", message = "Поддерживаются только языки EN и RU")
        String language
) {
}
