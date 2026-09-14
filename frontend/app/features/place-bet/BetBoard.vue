<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import AppButton from '~/shared/ui/AppButton.vue'
import PuzzleFragment from '~/features/place-bet/PuzzleFragment.vue'
import HistoryFeed from '~/widgets/HistoryFeed.vue'
import RulesModal from '~/features/RulesModal.vue'
import type { ThemeId } from '~/shared/types/game'

const store = useGameStore()
const rulesOpen = ref(false)
const activating = ref(false)

const canStart = computed(
  () => !!store.selectedBet && store.balance >= (store.selectedBet?.cost ?? Infinity),
)

const pick = (t: ThemeId) => {
  store.setTheme(t)
}

const start = async () => {
  if (!canStart.value || activating.value) return
  activating.value = true
  // короткая анимация активации фрагмента
  await new Promise((r) => setTimeout(r, 600))
  await store.startRound()
  activating.value = false
}
</script>

<template>
  <div class="flex-1 overflow-y-auto" :class="`theme-${store.theme}`">
    <div class="mx-auto flex w-full max-w-5xl flex-col gap-5 px-4 py-6">
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-extrabold">Сделай ставку</h1>
        <AppButton size="sm" variant="ghost" data-testid="rules-btn" @click="rulesOpen = true">
          Правила
        </AppButton>
      </div>

      <!-- Выбор шара -->
      <div class="grid grid-cols-2 gap-4" data-testid="theme-switch">
        <button
          class="card flex cursor-pointer flex-col items-center gap-2 p-5 font-bold transition-all"
          :class="store.theme === 'red' ? 'border-accent ring-2 ring-accent' : 'hover:border-accent'"
          data-testid="theme-red"
          @click="pick('red')"
        >
          <img src="/img/baloon11.png" alt="Красный шар" class="h-20 w-auto object-contain" />
          Красный
          <small class="font-normal text-muted">12 уровней</small>
        </button>
        <button
          class="card flex cursor-pointer flex-col items-center gap-2 p-5 font-bold transition-all"
          :class="store.theme === 'green' ? 'border-accent ring-2 ring-accent' : 'hover:border-accent'"
          data-testid="theme-green"
          @click="pick('green')"
        >
          <img src="/img/baloon22.png" alt="Зелёный шар" class="h-20 w-auto object-contain" />
          Зелёный
          <small class="font-normal text-muted">9 уровней</small>
        </button>
      </div>

      <div class="text-center text-sm text-muted" data-testid="level-count">
        Тема: {{ store.themeConfig?.name }} · уровней: <b class="text-accent-deep">{{ store.levelCount }}</b>
      </div>

      <!-- Фрагменты ставки -->
      <div class="grid grid-cols-2 gap-3 md:grid-cols-4" :class="activating ? 'animate-pulse-fast' : ''">
        <PuzzleFragment
          v-for="(opt, i) in store.betOptions"
          :key="opt.id"
          :option="opt"
          :index="i"
          :selected="store.selectedBetId === opt.id"
          :affordable="store.balance >= opt.cost"
          @select="store.selectBet(opt.id)"
        />
      </div>

      <div class="card px-4 py-3 text-center text-sm leading-relaxed text-muted">
        Бустер срабатывает, если шар пройдёт уровень с маркером
        <b class="text-accent-deep">до того, как ты забрал</b>, и умножит коэффициент.
      </div>

      <AppButton size="lg" block :disabled="!canStart || activating" data-testid="start-btn" @click="start">
        {{ activating ? 'Запуск…' : 'Начать' }}
      </AppButton>

      <HistoryFeed :items="store.history" />
    </div>

    <RulesModal :open="rulesOpen" @close="rulesOpen = false" />
  </div>
</template>
