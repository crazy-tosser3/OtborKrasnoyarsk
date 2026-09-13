<script setup lang="ts">
import type { BetOption } from '~/shared/types/game'

const props = defineProps<{
  option: BetOption
  selected: boolean
  affordable: boolean
  index: number
}>()
const emit = defineEmits<{ select: [] }>()

const label = computed(() =>
  props.option.boosterTier === 1 ? 'Без бустера' : `Бустер ×${props.option.boosterTier}`,
)

// Картинка фрагмента пазла (pazle1..4) по индексу опции.
const pieceImg = computed(() => `/img/pazle${props.index + 1}.png`)
</script>

<template>
  <button
    type="button"
    class="card relative flex w-full cursor-pointer flex-col items-center gap-2 px-3 py-4 transition-all"
    :class="[
      selected ? 'border-accent ring-2 ring-accent -translate-y-1' : 'hover:border-accent',
      !affordable && 'cursor-not-allowed opacity-40',
    ]"
    :data-testid="`bet-${option.id}`"
    :data-tier="option.boosterTier"
    @click="emit('select')"
  >
    <!-- Фрагмент пазла (картинка) + бейдж множителя -->
    <div class="relative flex h-16 w-16 items-center justify-center" :data-idx="index">
      <img :src="pieceImg" :alt="`Фрагмент ×${option.boosterTier}`" class="h-16 w-16 object-contain" />
      <span
        class="absolute -bottom-1 -right-1 rounded-full bg-accent px-1.5 text-sm font-extrabold text-ink ring-2 ring-white"
      >
        ×{{ option.boosterTier }}
      </span>
    </div>

    <div class="flex flex-col items-center leading-tight">
      <span class="text-base font-bold">
        {{ option.cost }} <small class="text-xs font-normal text-muted">бонусов</small>
      </span>
      <span class="text-xs text-muted">{{ label }}</span>
    </div>

    <span v-if="!affordable" class="absolute right-2.5 top-2.5 text-sm">🔒</span>
  </button>
</template>
