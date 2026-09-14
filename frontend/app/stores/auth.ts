import { defineStore } from 'pinia'

// Сессия хранится на клиенте: username — для профиля, token+role — для админ-эндпоинтов
// (бэкенд выдаёт JWT и роль в ответе login).
const KEY = 'balloon:username'
const TOKEN_KEY = 'balloon:token'
const ROLE_KEY = 'balloon:role'

export interface AdminUser {
  ID: number
  UserName: string
  UserEmail: string
  UserRole: string
}
export interface AdminGame {
  id: string
  user_name: string
  score: number
  played_at: string
}
export interface AdminReward {
  id: string
  name: string
  user_name: string
  claimed: boolean
}
export interface AdminTournament {
  ID: string // UUID
  name: string
  ends_at: string
}
export interface LeaderboardEntry {
  user_name: string
  score: number
}
// Конфигурация игры — /api/admin/game_config (структура зеркалит бэкенд-Config).
export interface GameConfigData {
  game_duration: number
  max_score: number
  min_play_time: number
  reward_name: string
  bonus_every: number
  bonus_score: number
  reward_chance: number
  bonus_reward_chance: number
  enable_rewards: boolean
  enable_bonuses: boolean
  allow_replay: boolean
  max_games_per_day: number
  score_multiplier: number
}
export interface GameHistoryEntry {
  id: string
  user_name: string
  score: number
  played_at: string
}

// Бэкенд отдаёт поля в разном регистре (ID + snake_case, где-то PascalCase).
// Нормализуем ответы админки, чтобы UI не падал и всё отображалось стабильно.
const pick = (o: any, ...keys: string[]): any => {
  for (const k of keys) if (o && o[k] != null) return o[k]
  return undefined
}
const normUser = (u: any): AdminUser => ({
  ID: pick(u, 'ID', 'id') ?? 0,
  UserName: pick(u, 'UserName', 'user_name', 'username', 'name') ?? '',
  UserEmail: pick(u, 'UserEmail', 'user_email', 'email') ?? '',
  UserRole: pick(u, 'UserRole', 'user_role', 'role') ?? 'user',
})
const normTournament = (t: any): AdminTournament => ({
  ID: String(pick(t, 'ID', 'id') ?? ''),
  name: pick(t, 'name', 'Name') ?? '',
  ends_at: pick(t, 'ends_at', 'endsAt', 'EndsAt', 'endedAt', 'ended_at', 'EndedAt') ?? '',
})
const normReward = (r: any): AdminReward => ({
  id: String(pick(r, 'id', 'ID') ?? ''),
  name: pick(r, 'name', 'Name') ?? '',
  user_name: pick(r, 'user_name', 'UserName', 'username') ?? '',
  claimed: Boolean(pick(r, 'claimed', 'Claimed')),
})
const normGame = (g: any): AdminGame => ({
  id: String(pick(g, 'id', 'ID') ?? ''),
  user_name: pick(g, 'user_name', 'UserName', 'username') ?? '',
  score: Number(pick(g, 'score', 'Score') ?? 0),
  played_at: pick(g, 'played_at', 'playedAt', 'PlayedAt') ?? '',
})

export const useAuthStore = defineStore('auth', () => {
  const username = ref<string | null>(null)
  const email = ref<string | null>(null)
  const token = ref<string | null>(null)
  const role = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loggedIn = computed(() => !!username.value)
  const isAdmin = computed(() => (role.value ?? '').toLowerCase() === 'admin')

  async function request(path: string, init?: RequestInit): Promise<any> {
    const base = useRuntimeConfig().public.userApiBase as string
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (token.value) headers.Authorization = `Bearer ${token.value}`
    const res = await fetch(`${base}${path}`, { headers, ...init })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      throw new Error((data && (data.error || data.message)) || `Ошибка ${res.status}`)
    }
    return data
  }

  async function loadProfile(name: string) {
    try {
      const data = await request(`/api/user/profile/${encodeURIComponent(name)}`)
      email.value = data?.user_email ?? null
    } catch {
      email.value = null
    }
  }

  function persistSession() {
    try {
      if (username.value) localStorage.setItem(KEY, username.value)
      if (token.value) localStorage.setItem(TOKEN_KEY, token.value)
      if (role.value) localStorage.setItem(ROLE_KEY, role.value)
    } catch {
      /* ignore */
    }
  }

  async function login(name: string, password: string) {
    loading.value = true
    error.value = null
    try {
      const data = await request('/api/user/login', {
        method: 'POST',
        body: JSON.stringify({ user_name: name, user_password: password }),
      })
      username.value = name
      token.value = data?.token ?? null
      role.value = data?.role ?? null
      persistSession()
      await loadProfile(name)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Ошибка входа'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function register(name: string, mail: string, password: string) {
    loading.value = true
    error.value = null
    try {
      await request('/api/user/register', {
        method: 'POST',
        body: JSON.stringify({ user_name: name, user_email: mail, user_password: password }),
      })
    } catch (e) {
      loading.value = false
      error.value = e instanceof Error ? e.message : 'Ошибка регистрации'
      throw e
    }
    loading.value = false
    // После успешной регистрации сразу входим тем же паролем.
    await login(name, password)
  }

  function logout() {
    username.value = null
    email.value = null
    token.value = null
    role.value = null
    try {
      localStorage.removeItem(KEY)
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ROLE_KEY)
    } catch {
      /* ignore */
    }
  }

  function clearError() {
    error.value = null
  }

  /** Восстановление сессии из localStorage при загрузке приложения. */
  function init() {
    let saved: string | null = null
    try {
      saved = localStorage.getItem(KEY)
      token.value = localStorage.getItem(TOKEN_KEY)
      role.value = localStorage.getItem(ROLE_KEY)
    } catch {
      /* ignore */
    }
    if (saved) {
      username.value = saved
      void loadProfile(saved)
    }
  }

  // --- админ-панель (только для role === 'admin') ---
  const adminUsers = ref<AdminUser[]>([])
  const adminGames = ref<AdminGame[]>([])
  const adminRewards = ref<AdminReward[]>([])
  const adminTournaments = ref<AdminTournament[]>([])
  const adminLoading = ref(false)
  const adminError = ref<string | null>(null)

  // Кол-во наград (пазлов) на каждого пользователя — GET /api/rewards?username=
  // Бэкенд отдаёт только по одному username за раз, поэтому бьём список
  // пользователей на отдельные запросы (allSettled: один упавший не рушит остальные).
  const adminUserRewardCounts = ref<Record<string, number>>({})

  async function loadAdminUserRewardCounts(userNames: string[]) {
    const results = await Promise.allSettled(
      userNames.map((name) => request(`/api/rewards?username=${encodeURIComponent(name)}`)),
    )
    const counts: Record<string, number> = {}
    results.forEach((res, i) => {
      const name = userNames[i]!
      counts[name] = res.status === 'fulfilled' && Array.isArray(res.value) ? res.value.length : 0
    })
    adminUserRewardCounts.value = counts
  }

  // --- конфиг игры (/api/admin/game_config) ---
  const adminGameConfig = ref<GameConfigData | null>(null)
  const adminGameConfigLoading = ref(false)
  const adminGameConfigError = ref<string | null>(null)
  const adminGameConfigSaving = ref(false)

  async function loadGameConfig() {
    adminGameConfigLoading.value = true
    adminGameConfigError.value = null
    try {
      adminGameConfig.value = await request('/api/admin/game_config')
    } catch (err) {
      adminGameConfigError.value = err instanceof Error ? err.message : 'Не удалось загрузить конфиг'
    } finally {
      adminGameConfigLoading.value = false
    }
  }

  async function adminUpdateGameConfig(patch: GameConfigData) {
    adminGameConfigSaving.value = true
    adminGameConfigError.value = null
    try {
      adminGameConfig.value = await request('/api/admin/game_config', {
        method: 'PUT',
        body: JSON.stringify(patch),
      })
    } catch (err) {
      adminGameConfigError.value = err instanceof Error ? err.message : 'Не удалось сохранить конфиг'
      throw err
    } finally {
      adminGameConfigSaving.value = false
    }
  }

  async function loadAdminData() {
    adminLoading.value = true
    adminError.value = null
    // allSettled: неудача одного эндпоинта не обнуляет остальные секции.
    // Турниры берём из публичного /api/tournament (там их отдаёт GetTournaments).
    const [users, games, rewards, tournaments] = await Promise.allSettled([
      request('/api/admin/users'),
      request('/api/admin/games'),
      request('/api/admin/rewards'),
      request('/api/tournament'),
    ])
    const toArray = (v: any) => (Array.isArray(v) ? v : v ? [v] : [])
    adminUsers.value = users.status === 'fulfilled' ? toArray(users.value).map(normUser) : []
    adminGames.value = games.status === 'fulfilled' ? toArray(games.value).map(normGame) : []
    adminRewards.value = rewards.status === 'fulfilled' ? toArray(rewards.value).map(normReward) : []
    adminTournaments.value =
      tournaments.status === 'fulfilled' ? toArray(tournaments.value).map(normTournament) : []
    if (users.status === 'rejected') {
      adminError.value = users.reason instanceof Error ? users.reason.message : 'Не удалось загрузить данные'
    }
    adminLoading.value = false
    // Конфиг — отдельный запрос: его неудача не должна ронять остальную панель.
    void loadGameConfig()
    // Кол-во наград по каждому пользователю — тоже отдельно и не блокирует остальное.
    if (adminUsers.value.length > 0) {
      void loadAdminUserRewardCounts(adminUsers.value.map((u) => u.UserName))
    }
  }

  async function adminChangeRole(userName: string, newRole: string) {
    await request('/api/admin/users/role', {
      method: 'PUT',
      body: JSON.stringify({ user_name: userName, role: newRole }),
    })
    await loadAdminData()
  }

  async function adminDeleteUser(userName: string) {
    await request(`/api/admin/users/${encodeURIComponent(userName)}`, { method: 'DELETE' })
    await loadAdminData()
  }

  async function adminCreateReward(name: string, userName: string) {
    await request('/api/admin/rewards', {
      method: 'POST',
      body: JSON.stringify({ name, user_name: userName }),
    })
    await loadAdminData()
  }

  // Бэкенд ждёт CreateTournamentRequest = { name, startedAt, endedAt } (camelCase).
  async function adminCreateTournament(name: string, startedAt: string, endedAt: string) {
    await request('/api/admin/tournaments', {
      method: 'POST',
      body: JSON.stringify({ name, startedAt, endedAt }),
    })
    await loadAdminData()
  }

  async function adminDeleteTournament(id: string) {
    await request(`/api/admin/tournaments/${encodeURIComponent(id)}`, { method: 'DELETE' })
    await loadAdminData()
  }

  // --- турниры (публично, без токена) ---
  // Эндпоинт /api/tournament отдаёт СПИСОК всех активных турниров (массив), а не один.
  const tournaments = ref<AdminTournament[]>([])
  const tournamentLoading = ref(false)

  async function loadTournament() {
    tournamentLoading.value = true
    try {
      const data = await request('/api/tournament')
      // Бэкенд может вернуть массив либо один объект — нормализуем в список.
      const list = Array.isArray(data) ? data : data ? [data] : []
      tournaments.value = list.map(normTournament)
    } catch {
      tournaments.value = []
    } finally {
      tournamentLoading.value = false
    }
  }

  // --- таблица лидеров: реальные игроки из БД (публично, без токена) ---
  const leaderboardTop = ref<LeaderboardEntry[]>([])
  const leaderboardLoading = ref(false)

  async function loadLeaderboard() {
    leaderboardLoading.value = true
    try {
      const top = await request('/api/tournament/top')
      leaderboardTop.value = Array.isArray(top) ? top : []
    } catch {
      leaderboardTop.value = []
    } finally {
      leaderboardLoading.value = false
    }
  }

  // --- история игр пользователя (публично, без токена) ---
  // Бэкенд отдаёт общий список по всем игрокам — фильтруем на клиенте по имени.
  // null = запрос не удался (эндпоинт недоступен), [] = удался, но записей нет.
  const userGameHistory = ref<GameHistoryEntry[] | null>(null)

  async function loadUserGameHistory(userName: string) {
    try {
      const all = await request('/api/games/history/global')
      userGameHistory.value = Array.isArray(all)
        ? all.filter((g: GameHistoryEntry) => g.user_name === userName)
        : []
    } catch {
      userGameHistory.value = null
    }
  }

  // --- награды игрока (GET /api/rewards?username=) ---
  const userRewards = ref<AdminReward[]>([])

  async function loadUserRewards(userName: string) {
    try {
      const data = await request(`/api/rewards?username=${encodeURIComponent(userName)}`)
      userRewards.value = (Array.isArray(data) ? data : []).map(normReward)
    } catch {
      userRewards.value = []
    }
  }

  return {
    username,
    email,
    token,
    loading,
    error,
    loggedIn,
    isAdmin,
    login,
    register,
    logout,
    clearError,
    init,
    adminUsers,
    adminGames,
    adminRewards,
    adminTournaments,
    adminLoading,
    adminError,
    adminGameConfig,
    adminGameConfigLoading,
    adminGameConfigError,
    adminGameConfigSaving,
    loadGameConfig,
    adminUpdateGameConfig,
    adminUserRewardCounts,
    loadAdminData,
    adminChangeRole,
    adminDeleteUser,
    adminCreateReward,
    adminCreateTournament,
    adminDeleteTournament,
    tournaments,
    tournamentLoading,
    loadTournament,
    leaderboardTop,
    leaderboardLoading,
    loadLeaderboard,
    userGameHistory,
    loadUserGameHistory,
    userRewards,
    loadUserRewards,
  }
})
