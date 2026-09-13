<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useMinigameStore } from '~/stores/minigame'
import AppButton from '~/shared/ui/AppButton.vue'

const auth = useAuthStore()
const minigame = useMinigameStore()

// Динамическая таблица: тянем данные при входе и обновляем каждые 5 секунд.
let poll: ReturnType<typeof setInterval> | null = null
const refresh = () => {
  auth.loadLeaderboard()
  minigame.loadLeaderboard()
}

onMounted(() => {
  refresh()
  poll = setInterval(refresh, 5000)
})
onBeforeUnmount(() => {
  if (poll) clearInterval(poll)
})

const sorted = <T extends { score: number }>(list: T[]) => [...list].sort((a, b) => b.score - a.score)
const medal = (i: number) => ['🥇', '🥈', '🥉'][i] ?? `#${i + 1}`

// Заглушка для вёрстки блока "Лидеры за прошлый месяц".

const MOCK_LAST_MONTH_LEADERBOARD = [
  { user_name: 'nikita_pro', score: 48210 },
  { user_name: 'lera_k', score: 41750 },
  { user_name: 'maxvortex', score: 39980 },
  { user_name: 'anna.s', score: 35200 },
  { user_name: 'demo_user', score: 30110 },
]

const lastMonthLabel = (() => {
  const now = new Date()
  const prevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  return prevMonth.toLocaleString('ru-RU', { month: 'long', year: 'numeric' })
})()
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-6">
      <div class="flex items-center justify-between">
        <button
          class="cursor-pointer text-sm text-muted transition-colors hover:text-ink"
          data-testid="leaderboard-back"
          @click="navigateTo('/')"
        >
          ← Назад
        </button>
        <span class="flex items-center gap-1.5 text-xs text-muted">
          <span class="h-2 w-2 animate-pulse rounded-full bg-green-500" />
          Обновляется автоматически
        </span>
      </div>

      <h1 class="text-2xl font-extrabold">Лидеры</h1>

      <!-- Лидеры за прошлый месяц  -->
      <section class="card p-4" data-testid="leaderboard-last-month">
        <div class="mb-3 flex items-center gap-2 text-sm font-semibold text-muted">
          <span>Лидеры за {{ lastMonthLabel }}</span>
          
        </div>
        <div class="flex flex-col divide-y divide-line">
          <div
            v-for="(e, i) in sorted(MOCK_LAST_MONTH_LEADERBOARD)"
            :key="`lm-${e.user_name}-${i}`"
            class="flex items-center justify-between gap-3 py-3"
          >
            <div class="flex items-center gap-3">
              <img v-if="i === 0" src="/img/One1.jpg" alt="1 место" class="h-7 w-7 object-contain dark:invert" />
              <img v-else-if="i === 1" src="/img/Two2.jpg" alt="2 место" class="h-7 w-7 object-contain dark:invert" />
              <img v-else-if="i === 2" src="/img/Three3.jpg" alt="3 место" class="h-7 w-7 object-contain dark:invert" />
              <img v-else-if="i === 3" src="/img/Four4.jpg" alt="4 место" class="h-7 w-7 object-contain dark:invert" />
              <img v-else-if="i === 4" src="/img/Five5.jpg" alt="5 место" class="h-7 w-7 object-contain dark:invert" />
              <span v-else class="w-7 text-center text-lg">{{ medal(i) }}</span>
              <span class="font-semibold">{{ e.user_name }}</span>
            </div>
            <span class="font-bold text-accent-deep">
              {{ e.score }} <span class="text-sm font-normal text-muted">очков</span>
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
