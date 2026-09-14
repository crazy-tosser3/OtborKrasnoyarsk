<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import AppButton from '~/shared/ui/AppButton.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const auth = useAuthStore()
const mode = ref<'login' | 'register'>('login')
const username = ref('')
const email = ref('')
const password = ref('')

const inputClass =
  'w-full rounded-2xl border border-line px-4 py-2.5 text-sm outline-none transition-colors focus:border-accent'

const submit = async () => {
  try {
    if (mode.value === 'register') {
      await auth.register(username.value.trim(), email.value.trim(), password.value)
    } else {
      await auth.login(username.value.trim(), password.value)
    }
    password.value = ''
    emit('close')
  } catch {
    /* ошибка уже отражена в auth.error */
  }
}

const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  auth.clearError()
}
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
    data-testid="auth-modal"
    @click.self="emit('close')"
  >
    <div class="card w-full max-w-sm overflow-hidden">
      <div class="flex items-center justify-between bg-accent px-5 py-4 text-ink">
        <span class="text-lg font-extrabold">{{ mode === 'login' ? 'Вход' : 'Регистрация' }}</span>
        <button class="cursor-pointer text-xl text-ink/50 hover:text-ink" data-testid="auth-close" @click="emit('close')">
          ✕
        </button>
      </div>

      <form class="flex flex-col gap-3 p-5" @submit.prevent="submit">
        <label class="flex flex-col gap-1 text-xs font-medium text-muted">
          Имя пользователя
          <input v-model="username" data-testid="auth-username" required :class="inputClass" />
        </label>
        <label v-if="mode === 'register'" class="flex flex-col gap-1 text-xs font-medium text-muted">
          Email
          <input v-model="email" type="email" data-testid="auth-email" required :class="inputClass" />
        </label>
        <label class="flex flex-col gap-1 text-xs font-medium text-muted">
          Пароль
          <input v-model="password" type="password" data-testid="auth-password" required :class="inputClass" />
        </label>

        <div v-if="auth.error" class="text-xs text-[#c0362c]" data-testid="auth-error">{{ auth.error }}</div>

        <AppButton block :disabled="auth.loading" data-testid="auth-submit" @click="submit">
          {{ auth.loading ? 'Подождите…' : mode === 'login' ? 'Войти' : 'Зарегистрироваться' }}
        </AppButton>

        <button
          type="button"
          class="cursor-pointer text-xs text-muted underline hover:text-ink"
          data-testid="auth-toggle-mode"
          @click="toggleMode"
        >
          {{ mode === 'login' ? 'Нет аккаунта? Зарегистрироваться' : 'Уже есть аккаунт? Войти' }}
        </button>
      </form>
    </div>
  </div>
</template>
