<script setup lang="ts">
// Главный экран: обе игры в один ряд карточками-баннерами (стиль «Столото»).
// Перед запуском требуется вход: если игрок не в сети, открываем модалку и
// после входа автоматически ведём в выбранную игру.
import { useAuthStore } from '~/stores/auth'
import AuthModal from '~/features/AuthModal.vue'

const auth = useAuthStore()
const authOpen = ref(false)
const pending = ref<string | null>(null)

const games = [
  { path: '/balloon', title: 'Воздушный Шар', category: 'Crush-игры', banner: '/img/baner2.png' },
  { path: '/puzzle', title: 'Ловец пазлов', category: 'Shooter-игры', banner: '/img/baner1.png' },
]

const choose = (path: string) => {
  if (auth.loggedIn) return navigateTo(path)
  pending.value = path
  authOpen.value = true
}

// После успешного входа сразу ведём игрока в выбранную игру.
watch(
  () => auth.loggedIn,
  (ok) => {
    if (ok && pending.value) {
      const target = pending.value
      pending.value = null
      authOpen.value = false
      navigateTo(target)
    }
  },
)
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="mx-auto flex w-full max-w-5xl flex-col gap-6 px-4 py-8">
      <h1 class="text-2xl font-extrabold sm:text-3xl">Игры</h1>

      <!-- Обе игры в один ряд, формат карточек как на «Столото» -->
      <div class="flex flex-wrap gap-5">
        <div v-for="g in games" :key="g.path" class="flex w-72 max-w-full flex-col gap-2">
          <div class="text-sm font-bold text-muted">{{ g.category }}</div>

          <button
            type="button"
            class="group relative block aspect-[3/4] w-full overflow-hidden rounded-3xl shadow-lg transition-transform duration-200 hover:-translate-y-1"
            :data-testid="`game-${g.path.slice(1)}`"
            @click="choose(g.path)"
          >
            <img
              :src="g.banner"
              :alt="g.title"
              class="absolute inset-0 h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
            />
            <!-- Белая кнопка-плашка снизу во всю ширину (как у «Столото») -->
            <div class="absolute inset-x-3 bottom-3">
              <div class="rounded-2xl bg-white py-3 text-center text-sm font-extrabold text-[#16171b] shadow-md transition-transform group-hover:scale-[1.02]">
                {{ auth.loggedIn ? 'Играть' : 'Войти' }}
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>

    <AuthModal :open="authOpen" @close="authOpen = false" />
  </div>
</template>
