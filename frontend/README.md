# Воздушный Шар — игровой портал (кейс «Столото»)

- **Воздушный Шар** — crash-игра: коэффициент растёт, нужно успеть «Забрать» до того, как шар лопнет. Уровни (12 красный / 9 зелёный), бустеры, очки, история.
- **Ловец пазлов** — аркада на реакцию (canvas): лови фрагменты и золото, избегай бомб; счёт уходит в лидерборд.

Стек: **Nuxt 4 · Vue 3 · TypeScript · Pinia · Tailwind v4**, SPA (`ssr: false`). Игровая графика обеих игр рисуется на **HTML5 Canvas (2D-контекст)** — сцена шара с уровнями/бустерами и аркада «Ловец пазлов».

## Запуск

```bash
npm install
npm run dev  
```

## Архитектура (Feature-Sliced Design)

Слои идут сверху вниз: `pages → widgets → features → entities → shared`. Верхний слой
использует нижние, обратных зависимостей нет; кросс-слойные импорты — явные
(`import ... from '~/features/...'`); Nuxt авто-импортит только `components/` и `composables/`.

```
app/
  app.vue                 # <NuxtLayout><NuxtPage/> + инициализация сторов
  layouts/default.vue     # шапка + контент страницы + тост
  pages/                  # роуты-обёртки над features (+ гарды доступа)
  widgets/                # AppHeader (шапка), HistoryFeed
  features/               # экраны/сценарии: HomeBoard, place-bet/, play-round/,
                          # ResultView, PuzzleCatcher.client, Tournament/Leaderboard/
                          # Account/Admin, Auth/TopUp/Rules-модалки
  entities/balloon/       # графика сцены шара (renderer/sprites/palette)
  shared/
    ui/AppButton.vue      # базовая кнопка
    types/game.ts         # DTO
    config/game.config.ts # все числовые параметры «Шара»
    api/                  # GameApi (интерфейс) + mock/ (движок в браузере)
    lib/game/             # чистая математика: engine, prng, hash
  stores/                 # Pinia: game (Шар), auth (сессия+бэкенд+админка), minigame
  composables/useGameApi.ts
  plugins/gameApi.client.ts   # внедряет MockGameApi как $gameApi
public/img/               # логотип, баннеры игр, шары, пазлы, иконки
```

## Слой данных

- **Движок «Шара» — на клиенте** (как принято в crash-играх: раунд считается на фронте). Реализован за интерфейсом `GameApi` (`MockGameApi`), внедряется плагином как `$gameApi` и доступен через `useGameApi()`: генерирует точку краха/бустеры, считает очки и награды-пазлы, хранит баланс и историю в `localStorage`. Интерфейс `GameApi` позволяет при необходимости прозрачно подменить реализацию (например, серверную) в `plugins/gameApi.client.ts`.
- **Бэкенд** — сторы `auth` и `minigame` ходят на `NUXT_PUBLIC_USER_API_BASE` (единый `request()` с `Bearer`-токеном):
  `POST /api/minigame/start` → `{ session_id, secret, duration }`,
  `POST /api/minigame/finish` c `{ session_id, score, hash }`, где `hash = SHA256(session_id + score + secret)` (provably-fair); ответ финиша возвращает зачтённый счёт, факт награды и бонуса.
- Прочее: `POST /api/user/login|register`, `GET /api/tournament`, `GET /api/tournament/top`, `GET /api/minigame/leaderboard`, `GET /api/rewards?username=`, `…/api/admin/*` (пользователи, роли, турниры, награды, история игр, `game_config`).

## Ключевые моменты

- **Крах авторитетен к «серверу»**: UI не знает точку взрыва заранее, крах/выплату подтверждает реализация `GameApi`. «Забрать» фиксирует выигрыш, но шар долетает до взрыва — результат открывается после краха.
- **Награда-пазл**: за игру выдаётся один фрагмент (4 всего); при сборке 4 — бонус и алерт. Прогресс виден в личном кабинете.
- **Админская «подкрутка» (`/api/admin/game_config`)**: параметры «Ловца пазлов» задаёт админ-борд и применяет бэкенд — длительность раунда, множитель и лимит очков, лимит игр в день, шанс награды и бонус за серию. Результат виден игроку на экране финиша (зачтённый счёт, награда, бонус).
- **Доступ**: игры и `/account` — только после входа, `/admin` — только для роли `admin`.
- **Тема**: светлый стиль «Столото» (золото `#ffcd17`, шрифт Inter) + переключатель светлая/тёмная.
- **Тесты**: Vitest — математика движка и `MockGameApi`; Playwright — игровой цикл «Шара».
