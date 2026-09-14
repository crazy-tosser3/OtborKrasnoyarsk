<script setup lang="ts">
// MainLayout — общий каркас приложения: шапка сверху, контент страницы в <slot/>,
// глобальный тост снизу. Пока стор не готов — показываем заглушку загрузки.
import AppHeader from '~/widgets/AppHeader.vue'
import { useGameStore } from '~/stores/game'

const store = useGameStore()
</script>

<template>
  <div class="flex h-full flex-col bg-bg text-ink">
    <AppHeader />

    <main class="flex min-h-0 flex-1 flex-col">
      <slot v-if="store.ready" />
      <div v-else class="flex flex-1 items-center justify-center text-muted">Загрузка…</div>
    </main>

    <!-- Глобальный тост -->
    <transition name="toast">
      <div
        v-if="store.toast"
        :key="store.toast.id"
        class="pointer-events-none fixed bottom-6 left-1/2 z-[100] -translate-x-1/2 rounded-full bg-accent px-5 py-3 text-sm font-medium text-ink shadow-lg"
        data-testid="toast"
      >
        {{ store.toast.text }}
      </div>
    </transition>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 12px);
}
</style>
