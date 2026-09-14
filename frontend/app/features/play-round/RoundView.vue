<script setup lang="ts">
import { useGameStore } from '~/stores/game'
// Канвас только для браузера — подключаем явно (.client-компонент)
import GameCanvas from '~/features/play-round/GameCanvas.client.vue'

const store = useGameStore()

// Коэффициент над тёмным небом: белый на старте, жёлтый и крупнее с ростом.
const multClass = computed(() => {
  const m = store.displayMultiplier
  const color = m >= 2 ? 'text-accent' : 'text-white'
  return `${color} ${m >= 3 ? 'text-[58px]' : 'text-5xl'}`
})

// Живые очки за раунд (оценка для HUD).
const livePoints = computed(() => {
  const cfg = store.config
  if (!cfg) return 0
  let p = store.currentLevels * cfg.pointsPerLine
  if (store.cashoutInfo) {
    p += cfg.pointsCashoutBonus
    if (store.cashoutInfo.boosterApplied) p += cfg.pointsBoosterBonus
  }
  return p
})

const onKey = (e: KeyboardEvent) => {
  if ((e.code === 'Space' || e.code === 'Enter') && store.canCashout) {
    e.preventDefault()
    store.cashout()
  }
}
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="relative flex-1 overflow-hidden bg-black" :class="`theme-${store.theme}`">
    <GameCanvas />

    <!-- HUD поверх канваса -->
    <div class="pointer-events-none absolute inset-0 flex flex-col p-4">
      <div class="mt-2 text-center">
        <div
          class="font-extrabold leading-none transition-[font-size] duration-150"
          :class="multClass"
          data-testid="multiplier"
        >
          ×{{ store.displayMultiplier.toFixed(2) }}
        </div>
        <div
          v-if="store.boosterActive"
          class="mt-2 inline-block animate-bpop rounded-full bg-accent px-3 py-1 text-xs font-bold text-ink"
          data-testid="booster-active"
        >
          БУСТЕР ×{{ store.round?.boosterTier }}!
        </div>
      </div>

      <!-- всплывающие очки -->
      <div class="relative flex-1">
        <transition-group name="floater">
          <div
            v-for="f in store.floaters"
            :key="f.id"
            class="absolute left-1/2 top-[30%] -translate-x-1/2 text-lg font-extrabold text-white"
          >
            {{ f.text }}
          </div>
        </transition-group>
      </div>

      <div class="mt-auto flex items-end justify-between gap-3">
        <div class="pointer-events-none rounded-2xl bg-white/10 px-4 py-3 leading-snug backdrop-blur-sm" data-testid="live-win">
          <div class="text-xs text-white/60">Выигрыш</div>
          <div class="text-lg font-bold text-accent">
            {{ store.liveWinnings }} <small class="text-[10px] text-white/60">бонусов</small>
          </div>
          <div class="text-xs text-accent/90">{{ livePoints }} очков</div>
        </div>

        <div class="pointer-events-auto relative">
          <div
            v-if="store.showOnboarding"
            class="absolute bottom-full right-0 mb-3 w-48 animate-bob rounded-2xl bg-white px-4 py-2.5 text-xs font-medium text-[#16171b] shadow-lg"
            data-testid="onboarding"
          >
            Нажми «Забрать» до того, как шар лопнет ↓
          </div>
          <button
            type="button"
            class="btn btn--primary min-w-[150px]"
            :disabled="!store.canCashout"
            data-testid="cashout-btn"
            @click="store.cashout()"
          >
            {{ store.cashoutInfo ? `Забрано ×${store.cashoutInfo.exitMultiplier.toFixed(2)}` : 'Забрать' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.floater-enter-active {
  transition: all 1.1s ease-out;
}
.floater-enter-from {
  opacity: 1;
}
.floater-leave-to,
.floater-enter-to {
  opacity: 0;
  transform: translate(-50%, -60px);
}
</style>
