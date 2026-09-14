# Backend API

Backend-сервис для управления пользователями, турнирами, наградами и мини-игрой.

## Технологии

* Go 1.26+
* Gin
* PostgreSQL
* Redis
* GORM
* JWT
* Swagger

---

## Возможности

### Пользователи

* Регистрация
* Авторизация
* JWT-аутентификация
* Получение профиля
* Обновление данных пользователя
* Удаление пользователя

### Администрирование

* Просмотр списка пользователей
* Просмотр пользователя по username
* Изменение роли пользователя
* Удаление пользователя
* Создание турниров
* Удаление турниров
* Управление наградами
* Управление конфигурацией игры

### Турниры

* Получение списка турниров
* Получение последних турниров
* Таблица лидеров

### Награды

* Получение списка наград
* Получение награды пользователем
* Создание наград администратором

### Мини-игра

* Запуск игровой сессии
* Завершение игровой сессии
* Проверка игровых результатов
* Таблица лидеров мини-игры

---

## Структура проекта

```text
internal/
├── database/      # Подключение PostgreSQL и Redis
├── handlers/      # HTTP обработчики
├── middleware/    # JWT и Role middleware
├── models/        # Модели данных
└── service/       # Бизнес-логика

docs/              # Swagger документация
main.go            # Точка входа
```

---

## Переменные окружения

Создайте файл `.env` в корне проекта:

```env
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=password
DB_NAME=game_db
DB_SSLMODE=disable

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your_secret_key
```

---

## Запуск проекта

### Установка зависимостей

```bash
go mod download
```

### Запуск PostgreSQL

Убедитесь, что PostgreSQL доступен и база данных создана.

### Запуск Redis

Убедитесь, что Redis доступен по указанным настройкам.

### Запуск сервера

```bash
go run main.go
```

По умолчанию сервер запускается на:

```text
http://localhost:8000
```

---

## Swagger

После запуска документация доступна по адресу:

```text
http://localhost:8000/swagger/index.html
```

---

## Аутентификация

После успешного входа сервер возвращает JWT-токен:

```json
{
  "token": "jwt_token",
  "role": "admin"
}
```

Для защищённых маршрутов используйте заголовок:

```http
Authorization: Bearer <token>
```

---

## Основные маршруты

### Пользователи

| Метод  | Endpoint                      |
| ------ | ----------------------------- |
| POST   | `/api/user/register`          |
| POST   | `/api/user/login`             |
| POST   | `/api/user/update`            |
| DELETE | `/api/user/delete`            |
| GET    | `/api/user/profile/:username` |

### Турниры

| Метод | Endpoint                  |
| ----- | ------------------------- |
| GET   | `/api/tournament`         |
| GET   | `/api/latest_tournaments` |
| GET   | `/api/tournament/top`     |

### Награды

| Метод | Endpoint             |
| ----- | -------------------- |
| GET   | `/api/rewards`       |
| POST  | `/api/rewards/claim` |

### Мини-игра

| Метод | Endpoint                    |
| ----- | --------------------------- |
| POST  | `/api/minigame/start`       |
| POST  | `/api/minigame/finish`      |
| GET   | `/api/minigame/leaderboard` |

### Админ-панель

Все маршруты требуют:

```text
Authorization: Bearer <admin_token>
```

| Метод  | Endpoint                     |
| ------ | ---------------------------- |
| GET    | `/api/admin/users`           |
| GET    | `/api/admin/users/:username` |
| PUT    | `/api/admin/users/role`      |
| DELETE | `/api/admin/users/:username` |
| GET    | `/api/admin/tournaments`     |
| POST   | `/api/admin/tournaments`     |
| DELETE | `/api/admin/tournaments/:id` |
| GET    | `/api/admin/rewards`         |
| POST   | `/api/admin/rewards`         |
| GET    | `/api/admin/games`           |
| GET    | `/api/admin/game_config`     |
| PUT    | `/api/admin/game_config`     |

---

## Начальный администратор

При первом запуске автоматически создаётся администратор:

```text
Login: admin
Password: admin123
```

Рекомендуется изменить пароль сразу после развёртывания.

---

## База данных

При старте приложения автоматически выполняются миграции моделей:

* User
* GameHistory
* Tournament
* Reward
* MiniGameSession

Дополнительные миграции вручную выполнять не требуется.

---

## Безопасность

* Пароли хранятся в виде хэшей с использованием соли.
* Для авторизации используется JWT.
* Административные маршруты защищены Role Middleware.
* Проверка результатов мини-игры выполняется через серверную валидацию хэшей.

---
