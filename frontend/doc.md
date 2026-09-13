# API Documentation

## Base URL

```http
/api
```

Формат обмена данными: **JSON**

---

# Users

## Авторизация пользователя

### POST `/api/user/login`

Авторизация пользователя по логину и паролю.

### Request

```json
{
  "user_name": "player1",
  "user_password": "password123"
}
```

### Request Body

| Поле          | Тип    | Обязательно | Описание            |
| ------------- | ------ | ----------- | ------------------- |
| user_name     | string | Да          | Имя пользователя    |
| user_password | string | Да          | Пароль пользователя |

### Success Response (200)

```json
{
  "message": "login successful"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": "invalid request"
}
```

#### 401 Unauthorized

```json
{
  "error": "user not found"
}
```

или

```json
{
  "error": "wrong password"
}
```

---

## Регистрация пользователя

### POST `/api/user/register`

Создание нового пользователя.

### Request

```json
{
  "user_name": "player1",
  "user_password": "password123",
  "user_email": "player1@example.com"
}
```

### Request Body

| Поле          | Тип    | Обязательно | Описание           |
| ------------- | ------ | ----------- | ------------------ |
| user_name     | string | Да          | Имя пользователя   |
| user_password | string | Да          | Пароль             |
| user_email    | string | Да          | Email пользователя |

### Success Response (201)

```json
{
  "message": "user registered"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": "invalid request"
}
```

#### 409 Conflict

```json
{
  "error": "user already exists"
}
```

#### 500 Internal Server Error

```json
{
  "error": "cannot generate salt"
}
```

---

## Обновление пользователя

### POST `/api/user/update`

Изменение имени пользователя и пароля.

### Request

```json
{
  "user_name": "player1",
  "user_password": "old_password",
  "new_user_name": "player2",
  "new_user_password": "new_password"
}
```

### Request Body

| Поле              | Тип    |
| ----------------- | ------ |
| user_name         | string |
| user_password     | string |
| new_user_name     | string |
| new_user_password | string |

### Success Response (200)

```json
{
  "message": "user updated"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": "invalid request"
}
```

или

```json
{
  "error": "new username already exists"
}
```

#### 401 Unauthorized

```json
{
  "error": "wrong password"
}
```

#### 404 Not Found

```json
{
  "error": "user not found"
}
```

---

## Удаление пользователя

### DELETE `/api/user/delete`

Удаление пользователя после проверки логина и пароля.

### Request

```json
{
  "user_name": "player1",
  "user_password": "password123"
}
```

### Success Response (200)

```json
{
  "message": "user deleted"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": "invalid request"
}
```

#### 401 Unauthorized

```json
{
  "error": "wrong password"
}
```

#### 404 Not Found

```json
{
  "error": "user not found"
}
```

---

## Получение профиля пользователя

### GET `/api/user/profile/{username}`

Получение публичного профиля пользователя.

### Path Parameters

| Параметр | Тип    | Описание         |
| -------- | ------ | ---------------- |
| username | string | Имя пользователя |

### Example

```http
GET /api/user/profile/player1
```

### Success Response (200)

```json
{
  "user_name": "player1",
  "user_email": "player1@example.com"
}
```

### Error Response

#### 404 Not Found

```json
{
  "error": "user not found"
}
```

---

# Games

## История всех игр

### GET `/api/games/history/global`

Получение глобальной истории сыгранных матчей.

### Success Response (200)

```json
[
  {
    "id": "game-001",
    "user_name": "player1",
    "score": 1500,
    "played_at": "2025-09-01T15:30:00Z"
  }
]
```

### Response Fields

| Поле      | Тип     |
| --------- | ------- |
| id        | string  |
| user_name | string  |
| score     | integer |
| played_at | string  |

---

# Leaderboard

## Общий рейтинг игроков

### GET `/api/leaderboard`

Получение текущего рейтинга игроков.

### Success Response (200)

```json
[
  {
    "user_name": "player1",
    "score": 2500
  },
  {
    "user_name": "player2",
    "score": 2200
  }
]
```

### Response Fields

| Поле      | Тип     |
| --------- | ------- |
| user_name | string  |
| score     | integer |

---

# Tournament

## Получение информации о турнире

### GET `/api/tournament`

Возвращает данные активного турнира.

### Success Response (200)

```json
{
  "name": "Summer Championship",
  "ends_at": "2025-12-31T23:59:59Z"
}
```

### Response Fields

| Поле    | Тип    |
| ------- | ------ |
| name    | string |
| ends_at | string |

---

## Топ игроков турнира

### GET `/api/tournament/top`

Возвращает турнирный рейтинг игроков.

### Success Response (200)

```json
[
  {
    "user_name": "player1",
    "score": 3000
  }
]
```

---

## Live рейтинг турнира

### GET `/api/tournament/live`

Возвращает рейтинг турнира в реальном времени.

### Success Response (200)

```json
{
  "updated_at": "2025-09-01T15:30:00Z",
  "top": [
    {
      "user_name": "player1",
      "score": 3000
    }
  ]
}
```

### Response Fields

| Поле       | Тип                     |
| ---------- | ----------------------- |
| updated_at | datetime                |
| top        | array[LeaderboardEntry] |

---

# Rewards

## Получение инвентаря игрока

### GET `/api/rewards`

Возвращает список наград пользователя.

### Query Parameters

| Параметр | Тип    | Обязательный | Описание         |
| -------- | ------ | ------------ | ---------------- |
| username | string | Да           | Имя пользователя |

### Example

```http
GET /api/rewards?username=player1
```

### Success Response (200)

```json
[
  {
    "id": "reward-001",
    "name": "Golden Chest",
    "claimed": false,
    "user_name": "player1"
  }
]
```

### Response Fields

| Поле      | Тип     |
| --------- | ------- |
| id        | string  |
| name      | string  |
| claimed   | boolean |
| user_name | string  |

---

## Получение награды

### POST `/api/rewards/claim`

Помечает награду как полученную.

### Request

```json
{
  "reward_id": "reward-001"
}
```

### Request Body

| Поле      | Тип    | Описание              |
| --------- | ------ | --------------------- |
| reward_id | string | Идентификатор награды |

### Success Response (200)

```json
{
  "id": "reward-001",
  "name": "Golden Chest",
  "claimed": true,
  "user_name": "player1"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": "invalid request"
}
```

#### 404 Not Found

```json
{
  "error": "reward not found"
}
```

---

# Data Models

## UserProfile

```json
{
  "user_name": "string",
  "user_email": "string"
}
```

## GameHistory

```json
{
  "id": "string",
  "user_name": "string",
  "score": "integer",
  "played_at": "datetime"
}
```

## LeaderboardEntry

```json
{
  "user_name": "string",
  "score": "integer"
}
```

## Tournament

```json
{
  "name": "string",
  "ends_at": "datetime"
}
```

## Reward

```json
{
  "id": "string",
  "name": "string",
  "claimed": "boolean",
  "user_name": "string"
}
```
