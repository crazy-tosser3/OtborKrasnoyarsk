<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import AppButton from '~/shared/ui/AppButton.vue'

const auth = useAuthStore()

onMounted(() => {
  auth.loadAdminData()
})

const inputClass =
  'w-full rounded-2xl border border-line px-3 py-2 text-sm outline-none transition-colors focus:border-accent'

const newRewardName = ref('')
const newRewardUser = ref('')
const createReward = async () => {
  if (!newRewardName.value.trim() || !newRewardUser.value.trim()) return
  await auth.adminCreateReward(newRewardName.value.trim(), newRewardUser.value.trim())
  newRewardName.value = ''
  newRewardUser.value = ''
}

const newTournamentName = ref('')
const newTournamentStartsAt = ref('')
const newTournamentEndsAt = ref('')
const createTournament = async () => {
  if (!newTournamentName.value.trim() || !newTournamentEndsAt.value) return
  // Начало по умолчанию — сейчас, если не указано.
  const startedAt = new Date(newTournamentStartsAt.value || Date.now()).toISOString()
  const endedAt = new Date(newTournamentEndsAt.value).toISOString()
  await auth.adminCreateTournament(newTournamentName.value.trim(), startedAt, endedAt)
  newTournamentName.value = ''
  newTournamentStartsAt.value = ''
  newTournamentEndsAt.value = ''
}

const formatDate = (s: string) => {
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? s : d.toLocaleString('ru-RU')
}

// --- Конфиг игры (/api/admin/game_config) ---
import type { GameConfigData } from '~/stores/auth'

const configForm = ref<GameConfigData | null>(null)
const configSaved = ref(false)

// Локальная копия для формы — обновляем при каждой успешной загрузке с бэка,
// чтобы не перезаписывать то, что админ уже редактирует, случайным ре-фетчем.
watch(
  () => auth.adminGameConfig,
  (cfg) => {
    if (cfg && !configForm.value) configForm.value = { ...cfg }
  },
  { immediate: true },
)

const resetConfigForm = () => {
  if (auth.adminGameConfig) configForm.value = { ...auth.adminGameConfig }
}

const saveConfig = async () => {
  if (!configForm.value) return
  configSaved.value = false
  try {
    await auth.adminUpdateGameConfig(configForm.value)
    configSaved.value = true
    setTimeout(() => (configSaved.value = false), 2000)
  } catch {
    /* ошибка уже лежит в auth.adminGameConfigError */
  }
}
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-6">
      <div class="flex items-center justify-between">
        <button
          class="cursor-pointer text-sm text-muted transition-colors hover:text-ink"
          data-testid="admin-back"
          @click="navigateTo('/')"
        >
          ← Назад
        </button>
        <AppButton size="sm" variant="ghost" :disabled="auth.adminLoading" data-testid="admin-refresh" @click="auth.loadAdminData()">
          {{ auth.adminLoading ? 'Обновление…' : '↻ Обновить' }}
        </AppButton>
      </div>

      <h1 class="text-2xl font-extrabold">Админ-панель</h1>

      <div v-if="auth.adminError" class="card px-4 py-3 text-sm text-[#c0362c]" data-testid="admin-error">
        {{ auth.adminError }}
      </div>

      <!-- Пользователи -->
      <section class="card p-4" data-testid="admin-users">
        <div class="mb-3 text-sm font-semibold text-muted">
          Пользователи <span class="text-ink">({{ auth.adminUsers.length }})</span>
        </div>
        <div v-if="auth.adminUsers.length === 0" class="py-4 text-center text-sm text-muted">Нет данных</div>
        <div v-else class="flex flex-col gap-2">
          <div
            v-for="u in auth.adminUsers"
            :key="u.ID"
            class="flex flex-wrap items-center justify-between gap-2 rounded-2xl bg-surface px-4 py-2.5"
          >
            <div class="flex flex-col leading-tight">
              <span class="text-sm font-bold">
                {{ u.UserName }}
                <span class="ml-1 text-[10px] font-normal text-muted">{{ u.UserRole }}</span>
                <span
                  class="ml-1 rounded-full bg-accent/15 px-2 py-0.5 text-[10px] font-semibold text-accent-deep"
                  :data-testid="`admin-user-rewards-${u.UserName}`"
                  title="Кол-во наград (пазлов)"
                >
                  🧩 {{ auth.adminUserRewardCounts[u.UserName] ?? 0 }}
                </span>
              </span>
              <span class="text-[11px] text-muted">{{ u.UserEmail }}</span>
            </div>
            <div v-if="u.UserName !== auth.username" class="flex gap-2">
              <AppButton
                size="sm"
                variant="ghost"
                :data-testid="`admin-role-${u.UserName}`"
                @click="auth.adminChangeRole(u.UserName, (u.UserRole ?? '').toLowerCase() === 'admin' ? 'User' : 'admin')"
              >
                {{ (u.UserRole ?? '').toLowerCase() === 'admin' ? 'Разжаловать' : 'Сделать админом' }}
              </AppButton>
              <AppButton size="sm" variant="danger" :data-testid="`admin-delete-user-${u.UserName}`" @click="auth.adminDeleteUser(u.UserName)">
                Удалить
              </AppButton>
            </div>
            <span v-else class="text-[11px] text-muted">это вы</span>
          </div>
        </div>
      </section>

      <!-- Турниры -->
      <section class="card p-4" data-testid="admin-tournaments">
        <div class="mb-3 text-sm font-semibold text-muted">Турниры</div>
        <div v-if="auth.adminTournaments.length === 0" class="py-4 text-center text-sm text-muted">Нет турниров</div>
        <div v-else class="mb-3 flex flex-col gap-2">
          <div
            v-for="t in auth.adminTournaments"
            :key="t.ID"
            class="flex items-center justify-between gap-2 rounded-2xl bg-surface px-4 py-2.5"
          >
            <div class="flex flex-col leading-tight">
              <span class="text-sm font-bold">{{ t.name }}</span>
              <span class="text-[11px] text-muted">до {{ formatDate(t.ends_at) }}</span>
            </div>
            <AppButton size="sm" variant="danger" :data-testid="`admin-delete-tournament-${t.ID}`" @click="auth.adminDeleteTournament(t.ID)">
              Удалить
            </AppButton>
          </div>
        </div>
        <form class="flex flex-wrap items-end gap-2" @submit.prevent="createTournament">
          <label class="flex min-w-[160px] flex-1 flex-col gap-1 text-xs text-muted">
            Название
            <input v-model="newTournamentName" data-testid="admin-tournament-name" :class="inputClass" />
          </label>
          <label class="flex flex-col gap-1 text-xs text-muted">
            Начало
            <input v-model="newTournamentStartsAt" type="datetime-local" data-testid="admin-tournament-starts" :class="inputClass" />
          </label>
          <label class="flex flex-col gap-1 text-xs text-muted">
            Окончание
            <input v-model="newTournamentEndsAt" type="datetime-local" data-testid="admin-tournament-ends" :class="inputClass" />
          </label>
          <AppButton size="sm" data-testid="admin-tournament-create" @click="createTournament">Создать</AppButton>
        </form>
      </section>

      <!-- Награды -->
      <section class="card p-4" data-testid="admin-rewards">
        <div class="mb-3 text-sm font-semibold text-muted">Награды</div>
        <div v-if="auth.adminRewards.length === 0" class="py-4 text-center text-sm text-muted">Нет наград</div>
        <div v-else class="mb-3 flex flex-col gap-2">
          <div
            v-for="(r, i) in auth.adminRewards"
            :key="`${r.user_name}-${r.name}-${r.id || i}`"
            class="flex items-center justify-between gap-2 rounded-2xl bg-surface px-4 py-2.5"
          >
            <div class="flex flex-col leading-tight">
              <span class="text-sm font-bold">{{ r.name }}</span>
              <span class="text-[11px] text-muted">{{ r.user_name }}</span>
            </div>
            <span class="text-[11px] text-muted">{{ r.claimed ? 'получена' : 'не получена' }}</span>
          </div>
        </div>
        <form class="flex flex-wrap items-end gap-2" @submit.prevent="createReward">
          <label class="flex min-w-[160px] flex-1 flex-col gap-1 text-xs text-muted">
            Название
            <input v-model="newRewardName" data-testid="admin-reward-name" :class="inputClass" />
          </label>
          <label class="flex min-w-[160px] flex-1 flex-col gap-1 text-xs text-muted">
            Игрок (username)
            <input v-model="newRewardUser" data-testid="admin-reward-user" :class="inputClass" />
          </label>
          <AppButton size="sm" data-testid="admin-reward-create" @click="createReward">Выдать</AppButton>
        </form>
      </section>

      <!-- История игр -->
      <section class="card p-4" data-testid="admin-games">
        <div class="mb-3 text-sm font-semibold text-muted">
          История игр <span class="text-ink">({{ auth.adminGames.length }})</span>
        </div>
        <div v-if="auth.adminGames.length === 0" class="py-4 text-center text-sm text-muted">Нет данных</div>
        <div v-else class="flex flex-col gap-2">
          <div
            v-for="g in auth.adminGames"
            :key="g.id"
            class="flex items-center justify-between gap-2 rounded-2xl bg-surface px-4 py-2.5"
          >
            <span class="text-sm font-bold">{{ g.user_name }}</span>
            <span class="text-sm">{{ g.score }} очков</span>
            <span class="text-[11px] text-muted">{{ formatDate(g.played_at) }}</span>
          </div>
        </div>
      </section>

      <!-- Конфигурация игры -->
      <section class="card p-4" data-testid="admin-game-config">
        <div class="mb-3 flex items-center justify-between">
          <div class="text-sm font-semibold text-muted">Конфигурация игры</div>
          <AppButton
            size="sm"
            variant="ghost"
            :disabled="auth.adminGameConfigLoading"
            data-testid="admin-config-refresh"
            @click="auth.loadGameConfig()"
          >
            {{ auth.adminGameConfigLoading ? 'Обновление…' : '↻ Обновить' }}
          </AppButton>
        </div>

        <div v-if="auth.adminGameConfigError" class="mb-3 text-sm text-[#c0362c]" data-testid="admin-config-error">
          {{ auth.adminGameConfigError }}
        </div>

        <div v-if="!configForm" class="py-4 text-center text-sm text-muted">Загрузка…</div>
        <form v-else class="flex flex-col gap-4" @submit.prevent="saveConfig">
          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label class="flex flex-col gap-1 text-xs text-muted">
              Длительность раунда (сек)
              <input
                v-model.number="configForm.game_duration"
                type="number"
                min="0"
                data-testid="admin-config-game-duration"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Максимальный счёт
              <input
                v-model.number="configForm.max_score"
                type="number"
                min="0"
                data-testid="admin-config-max-score"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Мин. время игры (сек)
              <input
                v-model.number="configForm.min_play_time"
                type="number"
                min="0"
                data-testid="admin-config-min-play-time"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Название награды
              <input v-model="configForm.reward_name" data-testid="admin-config-reward-name" :class="inputClass" />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Бонус каждые N очков
              <input
                v-model.number="configForm.bonus_every"
                type="number"
                min="0"
                data-testid="admin-config-bonus-every"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Очков за бонус
              <input
                v-model.number="configForm.bonus_score"
                type="number"
                min="0"
                data-testid="admin-config-bonus-score"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Шанс награды (0–1)
              <input
                v-model.number="configForm.reward_chance"
                type="number"
                min="0"
                max="1"
                step="0.01"
                data-testid="admin-config-reward-chance"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Шанс бонусной награды (0–1)
              <input
                v-model.number="configForm.bonus_reward_chance"
                type="number"
                min="0"
                max="1"
                step="0.01"
                data-testid="admin-config-bonus-reward-chance"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Максимум игр в день
              <input
                v-model.number="configForm.max_games_per_day"
                type="number"
                min="0"
                data-testid="admin-config-max-games-per-day"
                :class="inputClass"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs text-muted">
              Множитель очков
              <input
                v-model.number="configForm.score_multiplier"
                type="number"
                min="0"
                step="0.01"
                data-testid="admin-config-score-multiplier"
                :class="inputClass"
              />
            </label>
          </div>

          <div class="flex flex-wrap gap-4">
            <label class="flex items-center gap-2 text-sm">
              <input v-model="configForm.enable_rewards" type="checkbox" data-testid="admin-config-enable-rewards" />
              Награды включены
            </label>
            <label class="flex items-center gap-2 text-sm">
              <input v-model="configForm.enable_bonuses" type="checkbox" data-testid="admin-config-enable-bonuses" />
              Бонусы включены
            </label>
            <label class="flex items-center gap-2 text-sm">
              <input v-model="configForm.allow_replay" type="checkbox" data-testid="admin-config-allow-replay" />
              Разрешить переигровку
            </label>
          </div>

          <div class="flex items-center gap-3">
            <AppButton size="sm" type="submit" :disabled="auth.adminGameConfigSaving" data-testid="admin-config-save">
              {{ auth.adminGameConfigSaving ? 'Сохранение…' : 'Сохранить' }}
            </AppButton>
            <AppButton size="sm" variant="ghost" type="button" data-testid="admin-config-cancel" @click="resetConfigForm">
              Отменить изменения
            </AppButton>
            <span v-if="configSaved" class="text-sm text-green-600" data-testid="admin-config-saved">Сохранено ✓</span>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>
