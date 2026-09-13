<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import { useAuthStore } from '~/stores/auth'

const store = useGameStore()
const auth = useAuthStore()

// Инициализация до монтирования страниц — чтобы гварды на /account и /admin
// уже видели актуальную сессию при первом рендере.
store.init()
auth.init()
</script>

<template>
  <div class="h-full">
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>
