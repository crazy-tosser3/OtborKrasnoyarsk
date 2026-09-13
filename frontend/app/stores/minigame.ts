import { defineStore } from 'pinia'
import { useAuthStore } from '~/stores/auth'
import { buildGameHash } from '~/shared/lib/game/hash'

// Мини-игра «Puzzle Catcher» на том же бэкенде, что и аккаунты/турниры.
// Эндпоинты: /api/minigame/start | /finish | /leaderboard.
export interface MinigameSession {
  session_id: string
  secret: string
  duration: number
}
export interface MinigameScore {
  user_name: string
  score: number
}
// Итог раунда с сервера. Поля отражают админскую «подкрутку»: score уже с
// учётом ScoreMultiplier/MaxScore, reward_created/bonus_granted — результат
// EnableRewards/RewardChance/EnableBonuses/BonusEvery.
export interface MinigameFinish {
  score: number
  reward_created: boolean
  bonus_granted: boolean
}

export const useMinigameStore = defineStore('minigame', () => {
  const auth = useAuthStore()

  const leaderboard = ref<MinigameScore[]>([])
  const leaderboardLoading = ref(false)

  // Тот же паттерн запроса, что и в auth-сторе: базовый URL из runtimeConfig,
  // токен в заголовке, единая обработка ошибок.
  async function request(path: string, init?: RequestInit): Promise<any> {
    const base = useRuntimeConfig().public.userApiBase as string
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (auth.token) headers.Authorization = `Bearer ${auth.token}`
    const res = await fetch(`${base}${path}`, { headers, ...init })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      throw new Error((data && (data.error || data.message)) || `Ошибка ${res.status}`)
    }
    return data
  }

  // Старт новой сессии — возвращает id и длительность раунда.
  async function startSession(): Promise<MinigameSession> {
    return request('/api/minigame/start', { method: 'POST' })
  }

  // Отправка финального счёта с provably-fair хэшем: SHA256(session_id + score + secret).
  // Возвращает итог сервера — уже с применённой «подкруткой» (множитель, лимит
  // очков, награда и бонус за серию).
  async function finishSession(sessionId: string, secret: string, score: number): Promise<MinigameFinish> {
    const hash = await buildGameHash(sessionId, score, secret)
    const res = await request('/api/minigame/finish', {
      method: 'POST',
      body: JSON.stringify({ session_id: sessionId, score, hash }),
    })
    return {
      score: Number(res?.score ?? score),
      reward_created: Boolean(res?.reward_created),
      bonus_granted: Boolean(res?.bonus_granted),
    }
  }

  async function loadLeaderboard() {
    leaderboardLoading.value = true
    try {
      leaderboard.value = (await request('/api/minigame/leaderboard')) ?? []
    } catch {
      leaderboard.value = []
    } finally {
      leaderboardLoading.value = false
    }
  }

  return {
    leaderboard,
    leaderboardLoading,
    startSession,
    finishSession,
    loadLeaderboard,
  }
})
