<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import { useAuthStore } from '~/stores/auth'
import BetBoard from '~/features/place-bet/BetBoard.vue'
import RoundView from '~/features/play-round/RoundView.vue'
import ResultView from '~/features/ResultView.vue'

const store = useGameStore()
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
  <BetBoard v-if="store.screen === 'bet'" />
  <RoundView v-else-if="store.screen === 'game'" />
  <ResultView v-else-if="store.screen === 'result'" />
</template>
