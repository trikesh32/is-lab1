# IS Lab 2 - Information Systems Laboratory Work

**Студент:** Трикашный Михаил Дмитриевич  
**Группа:** P3306  
**Вариант:** 228

## 📋 Описание

Лабораторная работа по информационным системам с реализацией:
- CRUD операций для сущности Person
- Массового импорта из CSV файлов
- Истории импорта с разграничением прав доступа
- Уникальных ограничений на уровне бизнес-логики
- Транзакций с уровнем изоляции SERIALIZABLE
- WebSocket для real-time обновлений

## 🛠 Технологии

### Backend
- Java 17
- Spring Boot 3.5.6
- PostgreSQL
- Spring Data JPA
- WebSocket (STOMP)
- Lombok

### Frontend
- Vue.js 3.5.22
- Vite
- Axios
- SockJS Client

## 🚀 Запуск проекта

### 1. База данных

Создайте PostgreSQL базу данных:
```sql
CREATE DATABASE postgres;
```

Настройте параметры подключения в `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

### 2. Backend

```bash
# Сборка проекта
./gradlew build

# Запуск
./gradlew bootRun
```

Backend будет доступен на `http://localhost:8080`

### 3. Frontend

```bash
cd front
npm install
npm run dev
```

Frontend будет доступен на `http://localhost:5173`

## 📤 Импорт данных

### Формат CSV файла

CSV файл должен содержать следующие колонки (в указанном порядке):
```
name,coord_x,coord_y,eyeColor,hairColor,loc_x,loc_y,loc_name,height,birthday,weight,nationality
```

### Пример строки:
```csv
Ivan Petrov,100.5,200.3,BLUE,BLACK,50.0,100,Moscow,180,2000-01-15T10:00:00+03:00,75,RUSSIA
```

### Допустимые значения:
- **eyeColor/hairColor**: BLACK, BLUE, YELLOW, ORANGE, WHITE
- **nationality**: RUSSIA, SPAIN, THAILAND
- **birthday**: ISO-8601 формат с временной зоной (например: `2000-01-15T10:00:00+03:00`)

### Использование:
1. Откройте веб-интерфейс
2. Введите username
3. Выберите CSV файл
4. Нажмите "Upload & Import"

Пример файла находится в `test-data/sample-import.csv`

## 🔒 Уникальные ограничения

На уровне бизнес-логики реализованы следующие ограничения уникальности:
- Комбинация: **имя + координаты (x, y) + дата рождения** должна быть уникальной
- Проверка выполняется как при создании через API, так и при импорте
- При нарушении ограничения транзакция откатывается полностью

## 🔐 Роли пользователей

### USER (обычный пользователь)
- Видит только свою историю импорта
- Может импортировать файлы
- Может выполнять CRUD операции

### ADMIN (администратор)
- Видит всю историю импорта всех пользователей
- Полный доступ ко всем операциям

### Создание администратора:
```bash
curl -X POST "http://localhost:8080/api/import/create-admin?username=admin&password=admin123"
```

## 🔄 Уровни изоляции транзакций

### READ_COMMITTED (по умолчанию)
Используется для операций чтения:
- `findAll()`
- `findById()`
- `findByNameContaining()`

### SERIALIZABLE
Используется для критических операций:
- `save()` - создание/обновление Person
- `deleteById()` - удаление Person
- `importPersonsFromCsv()` - импорт из CSV

**Обоснование:**
- SERIALIZABLE предотвращает фантомное чтение и гарантирует, что при одновременной попытке создать объекты с одинаковыми уникальными значениями только одна транзакция успешно завершится
- Защищает от race conditions при проверке уникальных ограничений
- Обеспечивает консистентность данных при параллельных операциях импорта

## 🧪 Тестирование с JMeter

### Установка JMeter
Скачайте Apache JMeter с [официального сайта](https://jmeter.apache.org/download_jmeter.cgi)

### Запуск тестов

```bash
# GUI режим (для разработки)
jmeter -t jmeter/IS-Lab2-Test-Plan.jmx

# CLI режим (для production)
jmeter -n -t jmeter/IS-Lab2-Test-Plan.jmx -l results.jtl -e -o report/
```

### Тестовые сценарии

План тестирования включает:

#### 1. Concurrent Create Operations (5 потоков)
- Одновременное создание объектов с одинаковыми уникальными значениями
- **Ожидаемый результат**: Только один объект создается успешно, остальные получают ошибку

#### 2. Concurrent Update Operations (3 потока)
- Одновременное обновление одного и того же объекта разными пользователями
- **Ожидаемый результат**: Последовательное применение изменений без потери данных

#### 3. Concurrent Delete Operations (2 потока)
- Одновременная попытка удалить один и тот же объект
- **Ожидаемый результат**: Один запрос успешен, второй получает 404

### Результаты тестирования

После выполнения тестов проверьте:
1. **View Results Tree** - детальные результаты каждого запроса
2. **Summary Report** - статистика по всем запросам
3. Логи приложения на наличие ошибок транзакций
4. Состояние базы данных на консистентность

## 📊 API Endpoints

### Person CRUD
- `GET /api/persons` - получить список (с пагинацией)
- `GET /api/persons/{id}` - получить по ID
- `POST /api/persons` - создать
- `PUT /api/persons/{id}` - обновить
- `DELETE /api/persons/{id}` - удалить

### Special Operations
- `GET /api/persons/operations/total-height` - сумма роста
- `GET /api/persons/operations/count-by-weight-less-than?weight={weight}` - подсчет по весу
- `GET /api/persons/operations/birthday-before?dateTime={date}` - поиск по дате рождения
- `GET /api/persons/operations/hair-color-percentage?hairColor={color}` - процент по цвету волос
- `GET /api/persons/operations/eye-color-percentage?eyeColor={color}` - процент по цвету глаз

### Import
- `POST /api/import/upload` - загрузить CSV файл
- `GET /api/import/history` - получить историю импорта
- `GET /api/import/history/{id}` - получить конкретную операцию импорта

### Admin
- `POST /api/import/create-admin` - создать администратора

## 🐛 Отладка

### Логи
Уровень логирования настроен в `application.properties`:
```properties
logging.level.com.trikesh.islab1=DEBUG
```

### Проблемы с транзакциями
Если возникают deadlock'и:
1. Проверьте логи PostgreSQL
2. Убедитесь, что используется правильный уровень изоляции
3. Проверьте порядок блокировки ресурсов

### WebSocket не работает
1. Убедитесь, что backend запущен на порту 8080
2. Проверьте CORS настройки в `WebSocketConfig`
3. Откройте консоль браузера для просмотра ошибок

## 📝 Структура проекта

```
is-labs/
├── src/main/java/com/trikesh/islab1/
│   ├── config/          # Конфигурация (CORS, WebSocket)
│   ├── controller/      # REST контроллеры
│   ├── model/           # Модели данных
│   ├── repository/      # JPA репозитории
│   ├── service/         # Бизнес-логика
│   └── DTO/            # Data Transfer Objects
├── front/
│   ├── src/
│   │   ├── components/  # Vue компоненты
│   │   └── services/    # API сервисы
│   └── public/
├── test-data/          # Тестовые данные
├── jmeter/             # JMeter тесты
└── README.md
```

## ✅ Выполненные требования

- ✅ Массовый импорт из CSV файлов
- ✅ Транзакционность импорта (rollback при ошибках)
- ✅ Валидация данных при импорте
- ✅ История импорта с разграничением доступа
- ✅ Уникальные ограничения на уровне бизнес-логики
- ✅ Уровень изоляции SERIALIZABLE для критических операций
- ✅ JMeter сценарии для тестирования параллельных операций
- ✅ Проверка корректности изоляции транзакций

## 📚 Дополнительная информация

### Обоснование уровней изоляции

**READ_COMMITTED** для чтения:
- Достаточно для операций, не изменяющих данные
- Лучшая производительность
- Предотвращает грязное чтение

**SERIALIZABLE** для записи:
- Необходим для проверки уникальных ограничений
- Предотвращает фантомное чтение
- Гарантирует консистентность при параллельных операциях
- Защищает от race conditions

### Результаты тестирования изоляции

При тестировании с JMeter было выявлено:
1. При использовании READ_COMMITTED возможны дубликаты при параллельном создании
2. SERIALIZABLE успешно предотвращает создание дубликатов
3. При параллельном обновлении изменения применяются последовательно
4. При параллельном удалении только одна операция успешна

## 🤝 Контакты

При возникновении вопросов обращайтесь к преподавателю или в техподдержку курса.