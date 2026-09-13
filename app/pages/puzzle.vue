<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import PuzzleCatcher from '~/features/PuzzleCatcher.client.vue'

const auth = useAuthStore()

// Игра доступна только вошедшим — иначе на главную к выбору/авторизации.
watch(
  () => auth.loggedIn,
  (ok) => {
    if (!ok) navigateTo('/')
  },
  { immediate: true },
)
</script>

<template>
  <PuzzleCatcher v-if="auth.loggedIn" />
</template>
