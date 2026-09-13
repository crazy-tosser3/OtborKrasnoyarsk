<script setup lang="ts">
import type { HistoryItem } from '~/shared/types/game'

const props = defineProps<{ items: HistoryItem[] }>()

const list = computed(() => props.items.slice(0, 20))
</script>

<template>
  <div class="card px-4 py-3" data-testid="history">
    <div class="mb-3 text-sm font-semibold text-muted">Прошлые игры</div>

    <div class="flex gap-2 overflow-x-auto pb-1">
      <div
        v-for="it in list"
        :key="it.id"
        class="min-w-[88px] flex-none rounded-2xl border bg-surface px-3 py-2 text-center leading-snug"
        :class="it.isSelf ? 'border-accent ring-1 ring-accent/40' : 'border-line'"
        :title="`${it.player}: крах ×${it.crashMultiplier}${it.exitMultiplier ? ', забрал ×' + it.exitMultiplier : ''}`"
      >
        <span class="block text-xs" :class="it.win ? 'text-accent-deep' : 'text-muted'">
          {{ it.win ? '✓' : '✕' }}
        </span>
        <span class="block text-base font-extrabold" :class="it.crashMultiplier >= 5 ? 'text-accent-deep' : ''">
          ×{{ it.crashMultiplier.toFixed(2) }}
        </span>
        <span v-if="it.win && it.exitMultiplier" class="block text-[10px] text-muted">
          забрал ×{{ it.exitMultiplier.toFixed(2) }}
        </span>
        <span class="mt-0.5 block truncate text-[10px]" :class="it.isSelf ? 'font-bold' : 'text-muted'">
          {{ it.isSelf ? 'Вы' : it.player }}
        </span>
      </div>
    </div>
  </div>
</template>
