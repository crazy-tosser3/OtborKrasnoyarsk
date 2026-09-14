<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import AppButton from '~/shared/ui/AppButton.vue'

const store = useGameStore()
const res = computed(() => store.result)

// Сколько бонусов можно было бы забрать на пике (в точке взрыва).
const maxWin = computed(() => (res.value ? Math.round(res.value.bet * res.value.crashMultiplier) : 0))
// Упущенная выгода = максимум минус то, что реально забрали.
const missed = computed(() => (res.value ? Math.max(0, maxWin.value - res.value.winnings) : 0))

let idleTimer: ReturnType<typeof setTimeout> | null = null
let countdown: ReturnType<typeof setInterval> | null = null
const secondsLeft = ref(10)

const armIdle = () => {
  const timeout = store.config?.resultIdleTimeout ?? 10000
  secondsLeft.value = Math.round(timeout / 1000)
  idleTimer = setTimeout(() => store.playAgain(), timeout)
  countdown = setInterval(() => {
    secondsLeft.value = Math.max(0, secondsLeft.value - 1)
  }, 1000)
}
const clearIdle = () => {
  if (idleTimer) clearTimeout(idleTimer)
  if (countdown) clearInterval(countdown)
}

onMounted(armIdle)
onBeforeUnmount(clearIdle)

</script>

<template>
  <div v-if="res" class="flex flex-1 items-center justify-center overflow-y-auto p-4" :class="`theme-${store.theme}`">
    <div class="card w-full max-w-md overflow-hidden" data-testid="result-card" :data-outcome="res.win ? 'win' : 'lose'">
      <!-- Заголовок результата -->
      <div
        class="px-5 py-4 text-center text-xl font-extrabold"
        :class="res.win ? 'bg-accent text-ink' : 'bg-[#c0362c] text-white'"
      >
        {{ res.win ? 'Вы забрали!' : 'Шар лопнул' }}
      </div>

      <div class="p-5 text-center">
        <img
          :src="res.win ? '/img/win.png' : '/img/lose.png'"
          :alt="res.win ? 'Победа' : 'Проигрыш'"
          class="mx-auto mb-3 h-24 w-24 object-contain"
        />
        <div v-if="res.win" class="mb-4 text-4xl font-extrabold text-accent-deep" data-testid="win-amount">
          +{{ res.winnings }} <small class="text-sm font-medium text-muted">бонусов</small>
        </div>
        <div v-else class="mb-4 text-xl font-bold text-[#c0362c]" data-testid="lose-amount">
          Ставка сгорела: −{{ res.bet }}
        </div>

        <!-- Показатели раунда -->
        <div class="mb-4 flex flex-col gap-2">
          <div class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Коэффициент краха</span>
            <b data-testid="crash-mult">×{{ res.crashMultiplier.toFixed(2) }}</b>
          </div>
          <div v-if="res.win" class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Забрали на</span>
            <b>×{{ res.exitMultiplier!.toFixed(2) }}</b>
          </div>
          <div v-if="res.win" class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Могли бы забрать</span>
            <b>{{ maxWin }} <span class="font-normal text-muted">бонусов · ×{{ res.crashMultiplier.toFixed(2) }}</span></b>
          </div>
          <div v-if="res.win && missed > 0" class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Упустили</span>
            <b class="text-[#c0362c]">{{ missed }} бонусов</b>
          </div>
          <div v-if="res.boosterApplied" class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Бустер</span>
            <b>×{{ res.boosterTier }} применён!</b>
          </div>
          <div class="flex justify-between rounded-2xl bg-surface px-4 py-2.5 text-sm">
            <span class="text-muted">Очки за раунд</span>
            <b class="text-accent-deep" data-testid="round-points">+{{ res.points }}</b>
          </div>
        </div>

        <!-- Награда -->
        <div class="mb-4 rounded-2xl bg-surface p-3" data-testid="reward">
          <div class="mb-2 text-xs text-muted">Награда: фрагмент пазла №{{ res.puzzlePiece + 1 }}</div>
          <div class="flex justify-center gap-2">
            <img
              v-for="i in 4"
              :key="i"
              :src="`/img/pazle${i}.png`"
              :alt="`Фрагмент ${i}`"
              class="h-11 w-11 object-contain transition-all"
              :class="i - 1 < res.puzzleCollected || i - 1 === res.puzzlePiece ? '' : 'opacity-25 grayscale'"
            />
          </div>
          <div v-if="res.puzzleBonus > 0" class="mt-2 text-sm font-bold" data-testid="puzzle-bonus">
            Пазл собран! +{{ res.puzzleBonus }} бонусов
          </div>
        </div>

        <div class="flex flex-col gap-2">
          <AppButton block size="lg" data-testid="again-btn" @click="store.playAgain()">
            Повторить ту же ставку
          </AppButton>
          <AppButton variant="ghost" block data-testid="repeat-btn" @click="store.repeatSame()">
            Играть снова
          </AppButton>
        </div>
        <div class="mt-3 text-xs text-muted">Автовозврат через {{ secondsLeft }} с</div>
      </div>
    </div>
  </div>
</template>
