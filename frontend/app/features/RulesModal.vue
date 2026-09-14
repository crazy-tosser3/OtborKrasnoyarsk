<script setup lang="ts">
import AppButton from '~/shared/ui/AppButton.vue'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const rules = [
  ['1. Ставка.', 'Выбери фрагмент пазла — это стоимость в бонусах и множитель бустера (×1…×4). Ставка списывается при старте.'],
  ['2. Полёт.', 'Шар взлетает, коэффициент растёт от ×1. Чем выше — тем больше выигрыш, но шар может лопнуть в любой момент.'],
  ['3. Уровни.', 'Горизонтальные линии — уровни. Красная тема — 12 уровней, зелёная — 9. За каждый пройденный уровень начисляются очки.'],
  ['4. Забрать.', 'Раунд заканчивается по нажатию «Забрать» (клавиша Space) — кнопка активна сразу после взлёта. Выигрыш = ставка × текущий коэффициент.'],
  ['5. Бустер.', 'При ставке ×2 и выше на одном из уровней стоит маркер бустера. Если шар прошёл его до того, как ты забрал — коэффициент умножается (скачок) плюс доп. очки.'],
  ['6. Крах.', 'Не успел забрать — ставка сгорает. Очки за пройденные уровни остаются.'],
  ['7. Награда.', 'За каждый раунд ты получаешь фрагмент пазла (4 разных). Собрал все 4 — бонус +500 бонусов, и пазл начинается заново.'],
]
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
    data-testid="rules-modal"
    @click.self="emit('close')"
  >
    <div class="card flex max-h-[88vh] w-full max-w-lg flex-col overflow-hidden">
      <div class="flex items-center justify-between bg-accent px-5 py-4 text-ink">
        <span class="text-lg font-extrabold">Как играть</span>
        <button class="cursor-pointer text-xl text-ink/50 hover:text-ink" data-testid="rules-close" @click="emit('close')">
          ✕
        </button>
      </div>

      <div class="overflow-y-auto p-5 text-sm leading-relaxed text-muted">
        <p v-for="[title, body] in rules" :key="title" class="mb-3">
          <b class="text-accent-deep">{{ title }}</b> {{ body }}
        </p>
      </div>

      <div class="border-t border-line p-4">
        <AppButton block @click="emit('close')">Понятно</AppButton>
      </div>
    </div>
  </div>
</template>
