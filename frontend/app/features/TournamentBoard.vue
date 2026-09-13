<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import AppButton from '~/shared/ui/AppButton.vue'

const auth = useAuthStore()

onMounted(() => {
  auth.loadTournament()
})

const formatDate = (s: string) => {
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? s : d.toLocaleString('ru-RU')
}
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-6">
      <div class="flex items-center justify-between">
        <button
          class="cursor-pointer text-sm text-muted transition-colors hover:text-ink"
          data-testid="tournament-back"
          @click="navigateTo('/')"
        >
          ← Назад
        </button>
        <AppButton
          size="sm"
          variant="ghost"
          :disabled="auth.tournamentLoading"
          data-testid="tournament-refresh"
          @click="auth.loadTournament()"
        >
          {{ auth.tournamentLoading ? 'Обновление…' : '↻ Обновить' }}
        </AppButton>
      </div>

      <h1 class="text-2xl font-extrabold">Турниры</h1>

      <!-- Список всех созданных турниров -->
      <div v-if="auth.tournaments.length" class="flex flex-col gap-3" data-testid="tournament-list">
        <div
          v-for="t in auth.tournaments"
          :key="t.ID"
          class="card flex items-center justify-between gap-3 p-5 transition-colors hover:border-accent"
          data-testid="tournament-item"
        >
          <div class="min-w-0">
            <div class="truncate text-base font-bold">{{ t.name }}</div>
            <div class="text-sm text-muted">до {{ formatDate(t.ends_at) }}</div>
          </div>
          <span class="flex-none rounded-full bg-accent/20 px-3 py-1 text-xs font-semibold text-accent-deep ring-1 ring-accent/40">Активен</span>
        </div>
      </div>

      <div v-else class="card p-8 text-center text-sm text-muted" data-testid="tournament-empty">
        {{ auth.tournamentLoading ? 'Загрузка…' : 'Турниры сейчас не проводятся' }}
      </div>
    </div>
  </div>
</template>
