# Backend API

Backend-сервис для управления пользователями, турнирами, наградами и мини-игрой.

## Технологии

* Java 25
* Spring Boot 4 (Web MVC, Security, Data JPA, Data Redis)
* PostgreSQL
* Redis
* JWT

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
* Создание и удаление турниров
* Управление наградами
* История игр
* Управление конфигурацией игры

### Турниры

* Активный турнир (хранится в Redis)
* Архив завершённых турниров
* Таблица лидеров турнира
* Автоматическое завершение турнира и определение победителя

### Награды

* Получение списка наград
* Получение награды пользователем
* Создание наград администратором

### Мини-игра

* Запуск игровой сессии
* Завершение игровой сессии
* Проверка игровых результатов по хэшу
* Таблица лидеров мини-игры

---

## Структура проекта

```text
src/main/java/com/example/balloon/
├── config/        # Security, JWT-фильтр, Redis, раздача фронтенда, создание админа
├── controller/    # HTTP-контроллеры
├── exception/     # Исключения и обработчик ошибок
├── model/
│   ├── dto/       # Запросы и ответы API
│   ├── entity/    # JPA-сущности
│   └── mapper/    # Преобразование сущностей в DTO
├── repository/    # JPA-репозитории и работа с Redis
└── service/       # Бизнес-логика
```

---

## Переменные окружения

Все параметры заданы в `application.properties` и переопределяются переменными окружения:

```env
SERVER_PORT=8000

DB_HOST=157.228.191.29
DB_PORT=9000
DB_NAME=game
DB_USER=postgres
DB_PASSWORD=postgres

REDIS_HOST=157.228.191.29
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DB=0

JWT_SECRET=your_secret_key_at_least_32_bytes_long
JWT_EXPIRATION=86400000
```

---

## Запуск проекта

### Локально

Нужны JDK 25, доступные PostgreSQL и Redis.

```bash
./mvnw spring-boot:run
```

### Docker

```bash
docker compose up -d --build
```

Поднимаются PostgreSQL, Redis и сервер приложения.

По умолчанию сервер запускается на:

```text
http://localhost:8000
```

---

## Фронтенд

Сервер отдаёт собранный фронтенд вместе с API. Соберите фронтенд в папке `frontend/` в корне проекта:

```bash
cd frontend
npm install
npm run generate
```

Содержимое `frontend/.output/public` при сборке попадает в jar. Неизвестные пути отдают `index.html`,
а несуществующие маршруты `/api/...` возвращают `404 {"error": "not found"}`.

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

| Метод | Endpoint                    |
| ----- | --------------------------- |
| GET   | `/api/tournament`           |
| GET   | `/api/latest_tournaments`   |
| GET   | `/api/tournament/top`       |
| GET   | `/api/games/history/global` |

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

## Хранилища

PostgreSQL — схема обновляется автоматически при старте (`ddl-auto=update`):

* users
* game_histories
* tournaments
* rewards

Redis:

* `tournament:active` — активный турнир
* `tournament:<id>:leaderboard` — таблица лидеров турнира
* `session:<id>` — игровые сессии (TTL 10 минут)
* `game:config` — конфигурация игры

---

## Безопасность

* Пароли хранятся в виде хэшей Argon2id с солью.
* Для авторизации используется JWT.
* Административные маршруты доступны только роли admin.
* Результат мини-игры проверяется на сервере через хэш `SHA256(session_id + score + secret)`.
