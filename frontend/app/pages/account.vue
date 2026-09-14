<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import AccountBoard from '~/features/AccountBoard.vue'

const auth = useAuthStore()

// Личный кабинет доступен только вошедшим — иначе на главную.
watch(
  () => auth.loggedIn,
  (ok) => {
    if (!ok) navigateTo('/')
  },
  { immediate: true },
)
</script>

<template>
  <AccountBoard v-if="auth.loggedIn" />
</template>
