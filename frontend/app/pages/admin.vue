<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import AdminBoard from '~/features/AdminBoard.vue'

const auth = useAuthStore()

// Админ-панель доступна только роли admin — иначе на главную.
watch(
  () => auth.isAdmin,
  (ok) => {
    if (!ok) navigateTo('/')
  },
  { immediate: true },
)
</script>

<template>
  <AdminBoard v-if="auth.isAdmin" />
</template>
