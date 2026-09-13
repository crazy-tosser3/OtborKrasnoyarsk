<script setup lang="ts">
// Кнопка-«таблетка» в чёрно-белой стилистике. Опирается на классы .btn/.btn--*
// из assets/styles/main.css, добавляя размеры и режим на всю ширину.
const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'ghost' | 'danger'
    disabled?: boolean
    block?: boolean
    size?: 'sm' | 'md' | 'lg'
  }>(),
  { variant: 'primary', disabled: false, block: false, size: 'md' },
)

const sizeClass: Record<string, string> = {
  sm: 'text-sm px-4 py-2',
  md: 'text-[15px]',
  lg: 'text-base px-7 py-4',
}
</script>

<template>
  <button
    type="button"
    :disabled="disabled"
    class="btn"
    :class="[`btn--${variant}`, sizeClass[size], block && 'w-full']"
  >
    <slot />
  </button>
</template>
