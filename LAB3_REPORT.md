# Лабораторная работа 3 - Отчет

## Выполненные задачи

### 1. Подключение к базе данных через Connection Pool (Apache Commons DBCP2)

#### Конфигурация пула соединений

В файле `application.properties` настроены следующие параметры DBCP2:

```properties
spring.datasource.type=org.apache.commons.dbcp2.BasicDataSource

spring.datasource.dbcp2.initial-size=5
spring.datasource.dbcp2.max-total=20
spring.datasource.dbcp2.max-idle=10
spring.datasource.dbcp2.min-idle=5
spring.datasource.dbcp2.max-wait-millis=30000
spring.datasource.dbcp2.validation-query=SELECT 1
spring.datasource.dbcp2.test-on-borrow=true
spring.datasource.dbcp2.test-while-idle=true
spring.datasource.dbcp2.time-between-eviction-runs-millis=60000
spring.datasource.dbcp2.min-evictable-idle-time-millis=300000
```

#### Описание параметров:

- **initial-size=5**: Начальное количество соединений при запуске пула
- **max-total=20**: Максимальное количество активных соединений в пуле
- **max-idle=10**: Максимальное количество простаивающих соединений
- **min-idle=5**: Минимальное количество простаивающих соединений
- **max-wait-millis=30000**: Максимальное время ожидания соединения (30 секунд)
- **validation-query=SELECT 1**: SQL-запрос для проверки валидности соединения
- **test-on-borrow=true**: Проверка соединения перед выдачей из пула
- **test-while-idle=true**: Проверка простаивающих соединений
- **time-between-eviction-runs-millis=60000**: Интервал проверки простаивающих соединений (60 секунд)
- **min-evictable-idle-time-millis=300000**: Минимальное время простоя перед удалением соединения (5 минут)

### 2. L2 JPA Cache с использованием Ehcache

#### Конфигурация Hibernate для L2 Cache

В `application.properties`:

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
spring.jpa.properties.hibernate.javax.cache.uri=classpath:ehcache.xml
spring.jpa.properties.hibernate.generate_statistics=true
```

#### Конфигурация Ehcache (ehcache.xml)

Созданы отдельные регионы кэша для каждой сущности:

**Person (Люди)**:
- TTL: 15 минут
- Heap: 500 записей
- Off-heap: 20 MB
- Стратегия: READ_WRITE

**User (Пользователи)**:
- TTL: 30 минут
- Heap: 100 записей
- Off-heap: 5 MB
- Стратегия: READ_WRITE

**ImportHistory (История импорта)**:
- TTL: 5 минут
- Heap: 200 записей
- Off-heap: 5 MB
- Стратегия: READ_WRITE

#### Уровни хранения кэша:

1. **Heap (On-heap)**: Хранение в памяти JVM. Быстрый доступ, но ограничен размером heap.
2. **Off-heap**: Хранение вне heap JVM. Больше памяти, не влияет на GC, но немного медленнее.

#### Влияние параметров на уровень хранения:

- **TTL (Time To Live)**: Определяет время жизни записи в кэше. Короткий TTL для часто изменяемых данных (ImportHistory), длинный для стабильных (User).
- **Heap entries**: Количество записей в быстрой памяти. Превышение лимита приводит к перемещению в off-heap.
- **Off-heap size**: Дополнительная память для кэша. Используется когда heap заполнен.

### 3. AOP для логирования статистики L2 Cache

Реализован аспект `CacheStatisticsAspect` с возможностью включения/отключения логирования:

#### Функциональность:

- Перехват всех вызовов методов репозиториев
- Логирование статистики кэша после каждой операции
- Динамическое включение/отключение через REST API

#### API endpoints:

```
POST /api/cache/statistics/enable  - Включить логирование
POST /api/cache/statistics/disable - Отключить логирование
GET  /api/cache/statistics/status  - Проверить статус
```

#### Логируемые метрики:

- Second Level Cache Hit Count (попадания в кэш)
- Second Level Cache Miss Count (промахи кэша)
- Second Level Cache Put Count (добавления в кэш)
- Query Cache Hit Count (попадания в кэш запросов)
- Query Cache Miss Count (промахи кэша запросов)
- Query Cache Put Count (добавления в кэш запросов)

### 4. Интеграция с MinIO (S3-совместимое хранилище)

#### Конфигурация MinIO

В `application.properties`:

```properties
minio.url=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=import-files
```

#### Функциональность MinioService:

- Автоматическое создание bucket при первом использовании
- Загрузка файлов с уникальными именами (UUID)
- Скачивание файлов
- Удаление файлов
- Проверка существования файлов

### 5. Двухфазный коммит (Two-Phase Commit)

#### Реализация распределенной транзакции

Создан сервис `TwoPhaseCommitService`, реализующий собственный двухфазный коммит:

#### Фаза 1 (Prepare):
1. Начало транзакции БД с уровнем изоляции SERIALIZABLE
2. Создание записи ImportHistory
3. Загрузка файла в MinIO
4. Сохранение пути к файлу в ImportHistory
5. Сохранение объектов Person в БД

#### Фаза 2 (Commit):
1. Коммит транзакции БД
2. Отправка WebSocket уведомлений

#### Обработка отказов:

**Отказ MinIO (БД работает)**:
- Откат транзакции БД
- Запись не создается в ImportHistory
- Файл не сохраняется

**Отказ БД (MinIO работает)**:
- Откат транзакции БД
- Удаление загруженного файла из MinIO
- Обновление статуса ImportHistory на FAILED

**RuntimeException в бизнес-логике**:
- Откат транзакции БД
- Удаление файла из MinIO
- Обновление статуса ImportHistory на FAILED

#### Обработка параллельных запросов:

1. **Уровень изоляции SERIALIZABLE**: Предотвращает фантомное чтение и аномалии параллельного доступа
2. **Уникальные UUID для файлов**: Исключает конфликты имен файлов в MinIO
3. **Транзакционная целостность**: Каждый импорт выполняется в отдельной транзакции
4. **Валидация уникальности**: Проверка бизнес-правил перед сохранением

#### Возможные ситуации при параллельных запросах:

**Ситуация 1: Одновременная загрузка одинаковых данных**
- Первая транзакция: Успешно сохраняет данные
- Вторая транзакция: Получает ошибку валидации уникальности, откатывается
- Результат: Данные сохранены один раз, файл второй транзакции удален

**Ситуация 2: Параллельная загрузка разных файлов**
- Обе транзакции выполняются независимо
- Каждая создает свою запись ImportHistory
- Файлы сохраняются с уникальными именами
- Результат: Обе операции успешны

**Ситуация 3: Отказ MinIO во время параллельных загрузок**
- Все активные транзакции откатываются
- Записи в БД не создаются
- Результат: Консистентное состояние системы

### 6. Скачивание файлов из истории импорта

#### Backend:

Endpoint: `GET /api/import/download/{id}`
- Получение файла из MinIO по ID записи ImportHistory
- Возврат файла с оригинальным именем
- Поддержка авторизации через JWT

#### Frontend:

- Добавлена кнопка "Скачать" в таблице истории импорта
- Автоматическое скачивание файла через браузер
- Обработка ошибок с уведомлениями пользователя

## Запуск приложения

### 1. Запуск инфраструктуры

```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL на порту 5432
- MinIO на портах 9000 (API) и 9001 (Console)

### 2. Доступ к MinIO Console

URL: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

### 3. Запуск backend

```bash
./gradlew bootRun
```

### 4. Запуск frontend

```bash
cd front
npm install
npm run dev
```

### 5. Остановка всех сервисов

```bash
docker-compose down
./gradlew --stop
```

## Тестирование распределенной транзакции

### Сценарий 1: Отказ MinIO

1. Остановить MinIO: `docker-compose stop minio`
2. Попытаться загрузить файл через UI
3. Проверить, что запись не создана в БД
4. Запустить MinIO: `docker-compose start minio`

### Сценарий 2: Отказ БД

1. Остановить PostgreSQL: `docker-compose stop postgres`
2. Попытаться загрузить файл через UI
3. Проверить, что файл не сохранен в MinIO
4. Запустить PostgreSQL: `docker-compose start postgres`

### Сценарий 3: RuntimeException

Для тестирования можно временно добавить `throw new RuntimeException("Test")` в метод `executeImport` после загрузки в MinIO, но до коммита БД.

## Зависимости

Добавлены в `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
implementation 'org.apache.commons:commons-dbcp2:2.12.0'
implementation 'org.hibernate.orm:hibernate-jcache:6.5.3.Final'
implementation 'org.ehcache:ehcache:3.10.8:jakarta'
implementation 'io.minio:minio:8.5.7'
```

## Выводы

1. **Connection Pool**: DBCP2 эффективно управляет соединениями с БД, предотвращая их исчерпание
2. **L2 Cache**: Ehcache значительно снижает нагрузку на БД за счет кэширования часто используемых данных
3. **AOP**: Позволяет гибко управлять логированием без изменения бизнес-логики
4. **MinIO**: Надежное хранилище для файлов с S3-совместимым API
5. **Two-Phase Commit**: Обеспечивает консистентность данных между БД и файловым хранилищем
6. **Параллельные запросы**: Правильная изоляция транзакций и валидация предотвращают конфликты