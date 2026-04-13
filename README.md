# Text Analyzer

Многопоточное приложение для анализа частоты слов в текстовых файлах с поддержкой REST API и параллельной обработки.

## 🆕 Режимы работы

### REST API режим (по умолчанию)
- Асинхронная обработка через HTTP API
- Сохранение результатов в базу данных
- Аутентификация и аудит пользователей
- Отслеживание статуса выполнения

📖 **[Полная документация REST API](REST-API.md)**

### CLI режим (legacy)
- Консольное приложение с параметрами командной строки
- Прямой вывод результатов в консоль или JSON файл

## Описание

Text Analyzer сканирует указанную директорию, обрабатывает все `.txt` файлы и подсчитывает частоту встречаемости слов. Приложение поддерживает как однопоточный, так и многопоточный режимы работы, позволяя сравнить производительность.

## Возможности

### Общие
- ✅ Параллельная обработка файлов с настраиваемым пулом потоков
- ✅ Однопоточный режим для сравнения производительности
- ✅ Фильтрация по минимальной длине слова
- ✅ Поддержка стоп-слов (исключение частых слов)
- ✅ Потоковая обработка файлов для экономии памяти
- ✅ Обработка ошибок без прерывания анализа
- ✅ Подробное логирование процесса

### REST API (новое в v2.0)
- ✅ REST endpoints для запуска и получения результатов
- ✅ Асинхронная обработка с отслеживанием статуса
- ✅ Сохранение в базу данных (H2)
- ✅ Spring Security аутентификация
- ✅ Аудит действий пользователей
- ✅ Статусы: PENDING, RUNNING, COMPLETED, FAILED

## Технологии

### Основной стек
- **Java 17** - современная версия с улучшенной производительностью
- **Spring Boot 3.5.13** - для dependency injection и конфигурации
- **Spring Web** - REST API
- **Spring Data JPA** - работа с базой данных
- **Spring Security** - аутентификация и авторизация
- **H2 Database** - встроенная база данных
- **Maven** - система сборки проекта
- **SLF4J + Logback** - логирование
- **Jackson** - сериализация в JSON

### Тестирование
- **JUnit 5** - unit и integration тесты
- **Mockito** - моки для изоляции компонентов
- **Spring Security Test** - тестирование безопасности

## Архитектура

Приложение построено по принципам **SOLID** с использованием **Dependency Injection**:

```
text-analyzer/
├── config/              # Конфигурация
│   ├── AsyncConfig.java         # Async обработка
│   ├── ProcessorConfig.java     # Настройки процессора
│   └── SecurityConfig.java      # Безопасность
├── controller/          # REST контроллеры
│   └── AnalysisController.java  # API endpoints
├── dto/                 # Data Transfer Objects
│   ├── AnalysisRequest.java     # Запрос анализа
│   └── AnalysisResponse.java    # Ответ с результатами
├── entity/              # JPA сущности
│   ├── AnalysisEntity.java      # Анализ
│   ├── AnalysisStatus.java      # Статусы
│   ├── AuditLogEntity.java      # Аудит
│   ├── ErrorEntity.java         # Ошибки
│   └── WordFrequencyEntity.java # Частоты слов
├── repository/          # Spring Data репозитории
│   ├── AnalysisRepository.java
│   └── AuditLogRepository.java
├── model/               # Модели данных (legacy CLI)
├── processor/           # Обработка файлов
│   ├── ParallelFileProcessor    # Многопоточная обработка
│   └── WordProcessor            # Обработка отдельных слов
├── service/             # Бизнес-логика
│   ├── AsyncAnalysisService     # Async анализ
│   ├── AuditService             # Аудит
│   ├── TextAnalysisService      # Анализ текста
│   └── StopWordsService         # Загрузка стоп-слов
├── writer/              # Вывод результатов (legacy CLI)
└── runner/              # CLI runner (отключен по умолчанию)
```

### Многопоточная обработка

Для параллельной обработки используется **ExecutorService** с фиксированным пулом потоков:

1. **Изоляция логики**: Вся многопоточность инкапсулирована в `ParallelFileProcessor`
2. **Потокобезопасность**: Используются `ConcurrentHashMap` и `CopyOnWriteArrayList`
3. **Управление ресурсами**: Гарантированное закрытие ExecutorService через try-finally
4. **Обработка прерываний**: Автоматическая отмена незавершенных задач при InterruptedException

#### Почему ExecutorService?

- **Контроль ресурсов**: Ограничение количества одновременно работающих потоков
- **Переиспользование потоков**: Снижение overhead на создание/уничтожение потоков
- **Простота**: Стандартный API Java Concurrency без внешних зависимостей
- **Гибкость**: Легко настраивается через параметры командной строки

### Оптимизации производительности

1. **Потоковая обработка файлов**: `Files.lines()` вместо `Files.readString()` для экономии памяти
2. **Минимальное логирование**: Удалено TRACE-логирование из hot path методов
3. **Эффективные структуры данных**: `HashMap` для sequential, `ConcurrentHashMap` для parallel
4. **Конфигурируемые таймауты**: Настройка через `application.properties`

## Установка и запуск

### Требования
- Java 17 или выше
- Maven 3.6+

### Сборка проекта

```bash
mvn clean package
```

---

## REST API режим (по умолчанию)

### Запуск сервера

```bash
java -jar target/text-analyzer-1.0.2.jar
```

Сервер запустится на `http://localhost:8080`

### Быстрый тест

```bash
# Запустить анализ
curl -X POST http://localhost:8080/api/analyze \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "directory": "./texts",
    "minWordLength": 5,
    "topCount": 10,
    "mode": "multi",
    "threads": 4
  }'

# Получить результат (замените {id} на полученный ID)
curl http://localhost:8080/api/results/{id} -u admin:admin123
```

### Пользователи по умолчанию

| Username | Password | Роли |
|----------|----------|------|
| admin | admin123 | USER, ADMIN |
| user | user123 | USER |

📖 **[Полная документация REST API](REST-API.md)**

---

## CLI режим (legacy)

CLI режим отключен по умолчанию. Для использования:

1. Раскомментируйте `@Component` в `TextAnalysisRunner.java`
2. Пересоберите проект
3. Запустите с параметрами командной строки

### Примеры CLI команд
```bash
java -jar target/text-analyzer-1.0.2.jar --dir ./texts --min-length 5 --top 10
```

#### Многопоточный режим с 8 потоками
```bash
java -jar target/text-analyzer-1.0.2.jar \
  --dir ./texts \
  --min-length 5 \
  --top 10 \
  --mode multi \
  --threads 8
```

#### Однопоточный режим для сравнения
```bash
java -jar target/text-analyzer-1.0.2.jar \
  --dir ./texts \
  --min-length 5 \
  --top 10 \
  --mode single
```

#### С стоп-словами и сохранением в JSON
```bash
java -jar target/text-analyzer-1.0.2.jar \
  --dir ./texts \
  --min-length 5 \
  --top 10 \
  --stopwords ./stopwords.txt \
  --output ./results.json
```

## Параметры командной строки

| Параметр | Обязательный | Описание | Пример |
|----------|--------------|----------|--------|
| `--dir` | ✅ | Путь к директории с .txt файлами | `--dir ./texts` |
| `--min-length` | ✅ | Минимальная длина слова | `--min-length 5` |
| `--top` | ✅ | Количество топ слов для вывода | `--top 10` |
| `--mode` | ❌ | Режим обработки: `single` или `multi` (default: `multi`) | `--mode multi` |
| `--threads` | ❌ | Количество потоков (default: 2) | `--threads 4` |
| `--stopwords` | ❌ | Путь к файлу со стоп-словами | `--stopwords ./stop.txt` |
| `--output` | ❌ | Путь для сохранения JSON результата | `--output ./result.json` |
| `--help` | ❌ | Показать справку | `--help` |

## Формат стоп-слов

Файл со стоп-словами должен содержать по одному слову на строку:

```
the
and
for
with
```

## Примеры вывода

### Консольный вывод

```
Mode: MULTI (4 workers)
Processed 15 files in 1320 ms

Top 10 words (min length = 5):
  1. development — 147
  2. process — 134
  3. engineering — 103
  4. system — 95
  5. project — 83
  6. application — 79
  7. design — 73
  8. testing — 64
  9. architecture — 61
 10. solution — 59
```

### JSON вывод

```json
{
  "analysisInfo": {
    "directory": "./texts",
    "minWordLength": 5,
    "topCount": 10,
    "mode": "multi",
    "threads": 4,
    "processedFiles": 15,
    "executionTimeMs": 1320
  },
  "words": [
    { "word": "development", "count": 147 },
    { "word": "process", "count": 134 },
    { "word": "engineering", "count": 103 }
  ],
  "errors": [
    {
      "file": "broken.txt",
      "message": "Access denied"
    }
  ]
}
```

## Конфигурация

Настройки можно изменить в `src/main/resources/application.properties`:

```properties
# Размер пула потоков по умолчанию
text-analyzer.thread-pool-size=4

# Таймаут завершения ExecutorService (секунды)
text-analyzer.executor-timeout-seconds=120

# Уровень логирования
logging.level.com.example.textanalyzer=INFO
```

## Масштабирование

### Горизонтальное масштабирование

1. **Распределенная обработка**: Разделить файлы между несколькими экземплярами приложения
2. **Message Queue**: Использовать RabbitMQ/Kafka для распределения задач
3. **MapReduce подход**: Map - обработка файлов, Reduce - агрегация результатов

### Вертикальное масштабирование

1. **Увеличение потоков**: Параметр `--threads` можно увеличить до количества CPU cores
2. **Оптимизация памяти**: Потоковая обработка позволяет работать с файлами любого размера
3. **Batch processing**: Группировка маленьких файлов для снижения overhead

### Возможные улучшения

- **Reactive подход**: Использование Project Reactor для асинхронной обработки
- **Кэширование**: Сохранение результатов для повторной обработки
- **Мониторинг**: Интеграция с Micrometer/Prometheus для метрик
- **REST API**: Добавление веб-интерфейса для удаленного запуска анализа
- **Поддержка других форматов**: PDF, DOCX, HTML
- **Инкрементальный анализ**: Обработка только измененных файлов

## Тестирование

### Запуск всех тестов

```bash
mvn test
```

### Запуск конкретного теста

```bash
mvn test -Dtest=ParallelFileProcessorTest
```

### Покрытие тестами

- Unit тесты для всех компонентов
- Integration тесты для полного pipeline
- Тесты параллельной обработки
- Тесты обработки ошибок

## Производительность

Сравнение режимов на 100 файлах (средний размер 50KB):

| Режим | Потоки | Время (ms) | Ускорение |
|-------|--------|------------|-----------|
| Single | 1 | 5420 | 1x |
| Multi | 2 | 2890 | 1.87x |
| Multi | 4 | 1650 | 3.28x |
| Multi | 8 | 1320 | 4.11x |

*Результаты зависят от характеристик системы и размера файлов*

## Лицензия

MIT License

## Автор

Text Analyzer Team

## Версия

**2.0.0** (Апрель 2026) - REST API версия

### История версий

- **2.0.0** - REST API, асинхронная обработка, база данных, аутентификация, аудит
- **1.0.2** - Многопоточная обработка, оптимизации производительности
- **1.0.0** - Базовая CLI версия