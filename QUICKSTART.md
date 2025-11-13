# 🚀 Быстрый старт

## Минимальные шаги для запуска проекта

### 1️⃣ База данных (PostgreSQL)

```bash
# Создайте БД (если еще не создана)
createdb postgres

# Или через psql
psql -U postgres
CREATE DATABASE postgres;
\q
```

### 2️⃣ Backend

```bash
# В корне проекта
./gradlew bootRun
```

Сервер запустится на `http://localhost:8080`

### 3️⃣ Frontend

```bash
cd front
npm install
npm run dev
```

Откройте `http://localhost:5173` в браузере

---

## 📤 Тестирование импорта

### Шаг 1: Откройте веб-интерфейс
Перейдите на `http://localhost:5173`

### Шаг 2: Загрузите тестовый файл
1. Введите username (например: `testuser`)
2. Выберите файл `test-data/sample-import.csv`
3. Нажмите "Upload & Import"

### Шаг 3: Проверьте результат
- Должно импортироваться 10 записей
- Проверьте таблицу Person
- Посмотрите историю импорта

---

## 🧪 Запуск JMeter тестов

```bash
# Убедитесь, что backend запущен
# Затем запустите JMeter

jmeter -t jmeter/IS-Lab2-Test-Plan.jmx
```

В GUI JMeter:
1. Нажмите зеленую кнопку "Start" (▶️)
2. Посмотрите результаты в "View Results Tree"
3. Проверьте статистику в "Summary Report"

---

## 🔧 Создание администратора

```bash
curl -X POST "http://localhost:8080/api/import/create-admin?username=admin&password=admin123"
```

Теперь можете войти как `admin` и видеть всю историю импорта.

---

## ✅ Проверка работоспособности

### API доступен?
```bash
curl http://localhost:8080/api/persons
```

Должен вернуть JSON с пустым списком или существующими записями.

### Frontend работает?
Откройте `http://localhost:5173` - должна загрузиться страница с таблицей.

### WebSocket работает?
Создайте Person через API или интерфейс - таблица должна обновиться автоматически.

---

## 🐛 Частые проблемы

### Backend не запускается
- Проверьте, что PostgreSQL запущен
- Проверьте пароль в `application.properties`
- Проверьте, что порт 8080 свободен

### Frontend не запускается
- Выполните `npm install` в папке `front`
- Проверьте версию Node.js (нужна 20+)
- Проверьте, что порт 5173 свободен

### Импорт не работает
- Проверьте формат CSV файла
- Убедитесь, что все обязательные поля заполнены
- Проверьте логи backend на наличие ошибок

---

## 📚 Дополнительная информация

Полная документация: [`README.md`](README.md)  
Анализ транзакций: [`docs/TRANSACTION_ISOLATION_ANALYSIS.md`](docs/TRANSACTION_ISOLATION_ANALYSIS.md)

---

**Готово!** Теперь можете работать с системой 🎉