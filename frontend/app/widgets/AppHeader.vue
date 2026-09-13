<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import { useAuthStore } from '~/stores/auth'
import AppButton from '~/shared/ui/AppButton.vue'
// Модалки из features подключаем явно — Nuxt их не авто-импортит
import AuthModal from '~/features/AuthModal.vue'
import TopUpModal from '~/features/TopUpModal.vue'

const store = useGameStore()
const auth = useAuthStore()

const authOpen = ref(false)
const topUpOpen = ref(false)

const balance = computed(() => String(store.balance).padStart(5, '0'))

// Переключатель тёмной/светлой темы.
const colorMode = useColorMode()
const toggleTheme = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}
</script>

<template>
  <header class="w-full border-b border-line bg-card">
    <nav class="flex w-full items-center gap-6 py-2.5 pl-2 pr-4 sm:pl-3 sm:pr-8">
      <!-- Логотип + надпись «Столото» у левого края -->
      <NuxtLink to="/" class="flex shrink-0 items-center gap-1">
        <img src="/img/logo.png" alt="Лотерея Игры" class="h-[68px] w-auto" />
        <img src="/img/logoLoto.png" alt="Столото" class="hidden h-9 w-auto sm:block dark:invert" />
      </NuxtLink>

      <!-- Навигация как в «Столото»: тёмные жирные ссылки -->
      <div class="hidden items-center gap-7 text-[15px] font-bold md:flex">
        <NuxtLink to="/" class="transition-colors hover:text-accent-deep">Игры</NuxtLink>
        <NuxtLink to="/tournament" class="transition-colors hover:text-accent-deep">Турнир</NuxtLink>
        <NuxtLink to="/leaderboard" class="transition-colors hover:text-accent-deep">Лидеры</NuxtLink>
      </div>

      <!-- Правый блок -->
      <div class="ml-auto flex items-center gap-2.5">
        <AppButton size="sm" variant="ghost" data-testid="theme-toggle" @click="toggleTheme">
          {{ colorMode.value === 'dark' ? '☀️' : '🌙' }}
        </AppButton>

        <template v-if="auth.loggedIn">
          <div class="flex items-center gap-2 rounded-full bg-accent/15 px-4 py-2 ring-1 ring-accent/40" data-testid="balance">
            <span class="text-xs text-muted">Бонусы</span>
            <span class="text-[15px] font-extrabold text-accent-deep">{{ balance }}</span>
          </div>
          <AppButton size="sm" variant="ghost" data-testid="topup" @click="topUpOpen = true">
            <img src="/img/plus.png" alt="" class="h-4 w-4" /> Пополнить
          </AppButton>
          <AppButton size="sm" variant="primary" data-testid="auth-btn" @click="navigateTo('/account')">
            <img src="/img/user.png" alt="" class="h-4 w-4" /> {{ auth.username }}
          </AppButton>
          <AppButton
            v-if="auth.isAdmin"
            size="sm"
            variant="danger"
            data-testid="admin-btn"
            @click="navigateTo('/admin')"
          >
            Админка
          </AppButton>
        </template>

        <!-- Не в сети: большая жёлтая кнопка как на stoloto.ru -->
        <button
          v-else
          type="button"
          class="btn btn--primary px-7 py-3.5 text-base font-extrabold"
          data-testid="auth-btn"
          @click="authOpen = true"
        >
          Вход и регистрация
        </button>
      </div>
    </nav>

    <AuthModal :open="authOpen" @close="authOpen = false" />
    <TopUpModal :open="topUpOpen" @close="topUpOpen = false" />
  </header>
</template>
