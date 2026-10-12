package com.dirinekyan.eft.domain;

public enum TaskStatus {
    /** Задача создана и ждёт обработки планировщиком. */
    NEW,
    /** Задача взята в работу. */
    IN_PROGRESS,
    /** Текст успешно исправлен. */
    COMPLETED,
    /** Обработка завершилась ошибкой (например, недоступен API Спеллера). */
    ERROR
}
