<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import AppButton from '~/shared/ui/AppButton.vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const store = useGameStore()

const quickAmounts = [200, 500, 1000, 2000]

const cardNumber = ref('')
const amount = ref(store.config?.topUpAmount ?? 500)
const submitting = ref(false)

const inputClass =
  'w-full rounded-2xl border border-line px-4 py-2.5 text-sm outline-none transition-colors focus:border-accent'

const onCardInput = (e: Event) => {
  const digits = (e.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 16)
  cardNumber.value = digits.replace(/(.{4})(?=.)/g, '$1 ')
}

const canSubmit = computed(
  () => cardNumber.value.replace(/\s/g, '').length === 16 && Number(amount.value) > 0,
)

const reset = () => {
  cardNumber.value = ''
  amount.value = store.config?.topUpAmount ?? 500
}

watch(
  () => props.open,
  (v) => {
    if (!v) reset()
  },
)

const submit = async () => {
  if (!canSubmit.value || submitting.value) return
  submitting.value = true
  try {
    await store.topUp(Number(amount.value))
    emit('close')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
    data-testid="topup-modal"
    @click.self="emit('close')"
  >
    <div class="card w-full max-w-sm overflow-hidden">
      <div class="flex items-center justify-between bg-accent px-5 py-4 text-ink">
        <span class="text-lg font-extrabold">Пополнение</span>
        <button class="cursor-pointer text-xl text-ink/50 hover:text-ink" data-testid="topup-close" @click="emit('close')">
          ✕
        </button>
      </div>

      <form class="flex flex-col gap-3 p-5" @submit.prevent="submit">
        <label class="flex flex-col gap-1 text-xs font-medium text-muted">
          Номер карты
          <input
            :value="cardNumber"
            inputmode="numeric"
            placeholder="0000 0000 0000 0000"
            maxlength="19"
            required
            data-testid="topup-card"
            :class="inputClass"
            @input="onCardInput"
          />
        </label>

        <label class="flex flex-col gap-1 text-xs font-medium text-muted">
          Сумма пополнения
          <input v-model.number="amount" type="number" min="1" step="1" required data-testid="topup-amount" :class="inputClass" />
        </label>

        <div class="flex gap-2">
          <button
            v-for="q in quickAmounts"
            :key="q"
            type="button"
            class="flex-1 cursor-pointer rounded-full border py-2 text-xs font-semibold transition-colors"
            :class="amount === q ? 'border-accent bg-surface' : 'border-line text-muted hover:border-accent'"
            :data-testid="`topup-quick-${q}`"
            @click="amount = q"
          >
            +{{ q }}
          </button>
        </div>

        <div class="text-xs text-muted">Демо-пополнение — данные карты никуда не отправляются.</div>

        <AppButton block :disabled="!canSubmit || submitting" data-testid="topup-submit" @click="submit">
          {{ submitting ? 'Пополняем…' : 'Пополнить' }}
        </AppButton>
      </form>
    </div>
  </div>
</template>
