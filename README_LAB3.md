# IS Labs - Лабораторная работа 3

## Новые возможности

### 1. Connection Pool (Apache Commons DBCP2)
- Эффективное управление соединениями с базой данных
- Настраиваемые параметры пула соединений
- Автоматическая валидация соединений

### 2. L2 JPA Cache (Ehcache)
- Кэширование сущностей второго уровня
- Кэширование запросов
- Настраиваемые регионы кэша для каждой сущности
- Поддержка heap и off-heap хранения

### 3. AOP для статистики кэша
- Динамическое включение/отключение логирования
- Детальная статистика использования кэша
- REST API для управления логированием

### 4. MinIO файловое хранилище
- S3-совместимое хранилище для импортированных файлов
- Автоматическое управление bucket
- Скачивание файлов из истории импорта

### 5. Двухфазный коммит
- Транзакционная целостность между БД и MinIO
- Автоматический откат при ошибках
- Поддержка параллельных запросов

## Быстрый старт

### Предварительные требования

- Java 17+
- Node.js 16+
- Docker и Docker Compose
- Gradle

### 1. Запуск инфраструктуры

```bash
docker-compose up -d
```

Это запустит:
- **PostgreSQL** на порту 5432
- **MinIO** на портах 9000 (API) и 9001 (Web Console)

### 2. Проверка MinIO

Откройте http://localhost:9001 в браузере:
- Username: `minioadmin`
- Password: `minioadmin`

### 3. Запуск Backend

```bash
./gradlew bootRun
```

Backend будет доступен на http://localhost:8080

### 4. Запуск Frontend

```bash
cd front
npm install
npm run dev
```

Frontend будет доступен на http://localhost:5173

## API Endpoints

### Cache Management

```
POST   /api/cache/statistics/enable   - Включить логирование статистики кэша
POST   /api/cache/statistics/disable  - Отключить логирование статистики кэша
GET    /api/cache/statistics/status   - Получить статус логирования
```

### Import

```
POST   /api/import/upload              - Загрузить CSV файл для импорта
GET    /api/import/history             - Получить историю импорта
GET    /api/import/history/{id}        - Получить детали импорта
GET    /api/import/download/{id}       - Скачать файл импорта
```

## Конфигурация

### Connection Pool (application.properties)

```properties
spring.datasource.dbcp2.initial-size=5
spring.datasource.dbcp2.max-total=20
spring.datasource.dbcp2.max-idle=10
spring.datasource.dbcp2.min-idle=5
spring.datasource.dbcp2.max-wait-millis=30000
```

### Ehcache (ehcache.xml)

Настройки кэша для каждой сущности:
- Person: TTL 15 минут, 500 записей в heap, 20MB off-heap
- User: TTL 30 минут, 100 записей в heap, 5MB off-heap
- ImportHistory: TTL 5 минут, 200 записей в heap, 5MB off-heap

### MinIO (application.properties)

```properties
minio.url=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=import-files
```

## Тестирование распределенной транзакции

### Сценарий 1: Отказ MinIO

```bash
docker-compose stop minio
```

Попробуйте загрузить файл через UI. Импорт должен завершиться ошибкой, запись в БД не должна быть создана.

```bash
docker-compose start minio
```

### Сценарий 2: Отказ БД

```bash
docker-compose stop postgres
```

Попробуйте загрузить файл через UI. Импорт должен завершиться ошибкой, файл не должен быть сохранен в MinIO.

```bash
docker-compose start postgres
```

### Сценарий 3: Параллельные запросы

Используйте Postman для отправки нескольких параллельных запросов импорта. Все запросы должны обрабатываться корректно без конфликтов.

## Мониторинг

### Логи кэша

После включения логирования статистики кэша через API, в логах приложения будут отображаться метрики:

```
=== Cache Statistics ===
Second Level Cache Hit Count: 150
Second Level Cache Miss Count: 25
Second Level Cache Put Count: 25
Query Cache Hit Count: 80
Query Cache Miss Count: 10
Query Cache Put Count: 10
========================
```

### MinIO Console

Доступ к веб-интерфейсу MinIO: http://localhost:9001

Здесь можно:
- Просматривать загруженные файлы
- Управлять bucket
- Мониторить использование хранилища

## Остановка приложения

### Остановка инфраструктуры

```bash
docker-compose down
```

Для удаления данных:

```bash
docker-compose down -v
```

### Остановка Backend

Нажмите `Ctrl+C` в терминале где запущен `./gradlew bootRun`

### Остановка Frontend

Нажмите `Ctrl+C` в терминале где запущен `npm run dev`

## Структура проекта

```
is-labs/
├── src/main/java/com/trikesh/islab1/
│   ├── aspect/
│   │   └── CacheStatisticsAspect.java      # AOP для логирования кэша
│   ├── config/
│   │   ├── MinioConfig.java                # Конфигурация MinIO
│   │   ├── SecurityConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/
│   │   ├── CacheController.java            # API управления кэшем
│   │   ├── ImportController.java           # API импорта с download
│   │   └── ...
│   ├── model/
│   │   ├── Person.java                     # @Cacheable сущность
│   │   ├── User.java                       # @Cacheable сущность
│   │   ├── ImportHistory.java              # @Cacheable сущность
│   │   └── ...
│   ├── service/
│   │   ├── MinioService.java               # Сервис работы с MinIO
│   │   ├── TwoPhaseCommitService.java      # Двухфазный коммит
│   │   ├── ImportService.java              # Обновленный сервис импорта
│   │   └── ...
│   └── ...
├── src/main/resources/
│   ├── application.properties              # Конфигурация DBCP2, Ehcache, MinIO
│   └── ehcache.xml                         # Конфигурация Ehcache
├── front/
│   └── src/components/
│       └── ImportHistory.vue               # Обновленный компонент с download
├── docker-compose.yml                      # PostgreSQL + MinIO
├── LAB3_REPORT.md                          # Подробный отчет
└── README_LAB3.md                          # Этот файл
```

## Troubleshooting

### MinIO не запускается

Проверьте, что порты 9000 и 9001 свободны:

```bash
lsof -i :9000
lsof -i :9001
```

### Ошибка подключения к MinIO

Убедитесь, что MinIO запущен:

```bash
docker-compose ps
```

Проверьте логи:

```bash
docker-compose logs minio
```

### Проблемы с кэшем

Проверьте логи Hibernate:

```properties
logging.level.org.hibernate.stat=DEBUG
```

### База данных не доступна

Проверьте статус PostgreSQL:

```bash
docker-compose ps postgres
```

Проверьте логи:

```bash
docker-compose logs postgres
```

## Дополнительная информация

Подробный отчет с описанием всех реализованных функций, конфигураций и тестовых сценариев находится в файле [LAB3_REPORT.md](LAB3_REPORT.md).