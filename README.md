# Error Free Text

REST-сервис, который исправляет опечатки в тексте с помощью [Яндекс.Спеллера](https://yandex.ru/dev/speller/). Пользователь создаёт задачу с текстом и языком, а фоновый планировщик проверяет текст и сохраняет исправленный вариант.

**Стек:** Java 17, Spring Boot 3, Gradle, PostgreSQL, Flyway, Docker / docker-compose, JUnit 5, Mockito.

## Как это работает

1. `POST /api/v1/tasks` сохраняет задачу в статусе `NEW` и возвращает её `id`.
2. Планировщик (`TaskProcessingScheduler`) с заданной периодичностью забирает пачку задач `NEW`, переводит их в `IN_PROGRESS` и отправляет текст в метод `checkTexts` Яндекс.Спеллера:
   - текст длиннее 10 000 символов делится на части по пробелам, слова не разрываются;
   - если в тексте есть цифры, включается `IGNORE_DIGITS`, если есть URL — `IGNORE_URLS`;
   - `FIND_REPEAT_WORDS` и `IGNORE_CAPITALIZATION` всегда выключены;
   - каждое найденное слово заменяется первым предложенным вариантом.
3. Если всё прошло успешно, задача получает статус `COMPLETED` и исправленный текст. Если обращение к API завершилось ошибкой, задача получает статус `ERROR` с описанием, и её обработка прекращается.
4. `GET /api/v1/tasks/{id}` возвращает результат.

## Запуск

```bash
docker compose up --build
```

Сервис будет доступен на `http://localhost:8080`. Для локального запуска без Docker нужен PostgreSQL (параметры в `application.yml` или в переменных `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`):

```bash
docker compose up -d db
./gradlew bootRun
```

Тесты:

```bash
./gradlew test
```

### Настройки

| Переменная | По умолчанию | Описание |
|---|---|---|
| `SCHEDULER_DELAY_MS` | `10000` | пауза между запусками планировщика |
| `SCHEDULER_BATCH_SIZE` | `10` | сколько задач обрабатывается за один запуск |
| `SPELLER_URL` | `https://speller.yandex.net/services/spellservice.json` | адрес API Спеллера |

## API

### Создать задачу

```http
POST /api/v1/tasks
Content-Type: application/json

{ "text": "Превет, как дила?", "language": "RU" }
```

Ответ `201 Created`:

```json
{ "id": "3f1c2a6e-8b0d-4a51-9a3e-2f6d6c1b7e10" }
```

Правила валидации:

- `language` принимает только `EN` или `RU`;
- `text` должен содержать минимум 3 символа и хотя бы одну букву: текст только из цифр и спецсимволов не принимается.

### Получить результат

```http
GET /api/v1/tasks/{id}
```

| Статус задачи | Ответ |
|---|---|
| завершена | `{ "status": "COMPLETED", "correctedText": "Привет, как дела?" }` |
| в обработке | `{ "status": "NEW" }` или `{ "status": "IN_PROGRESS" }` |
| ошибка | `{ "status": "ERROR", "errorMessage": "Ошибка обращения к Яндекс.Спеллеру: ..." }` |

### Формат ошибок

Все ошибки возвращаются в одном формате:

```json
{
  "errorMessage": "Поддерживаются только языки EN и RU",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-10-12T09:00:00Z",
  "path": "/api/v1/tasks"
}
```

| Код | HTTP | Когда |
|---|---|---|
| `VALIDATION_ERROR` | 400 | неверный язык или текст |
| `MALFORMED_REQUEST` | 400 | некорректный JSON |
| `INVALID_PARAMETER` | 400 | `id` не является UUID |
| `TASK_NOT_FOUND` | 404 | задача не найдена |
| `INTERNAL_ERROR` | 500 | непредвиденная ошибка |

## Структура

```
api/          контроллер и DTO
domain/       сущность CorrectionTask, статусы, языки
repository/   Spring Data JPA
service/      создание задач, исправление текста, планировщик
speller/      клиент Яндекс.Спеллера, разбиение текста, опции, применение исправлений
exception/    единый обработчик ошибок
```
