import { defineStore } from 'pinia'
import type {
  BetOption,
  CashoutSuccess,
  GameConfig,
  HistoryItem,
  Round,
  RoundResult,
  ThemeId,
} from '~/shared/types/game'
import { InsufficientBalanceError } from '~/shared/api/GameApi'
import { displayedMultiplier, levelsCrossed, multiplierAt } from '~/shared/lib/game/engine'
import { useMinigameStore } from '~/stores/minigame'

export type Screen = 'bet' | 'game' | 'result'
export type Phase = 'idle' | 'flying' | 'crashed'

interface Floater {
  id: number
  text: string
}

const ONBOARD_KEY = 'balloon:onboarded'

export const useGameStore = defineStore('game', () => {
  const api = useGameApi()
  // Бэкенд мини-игровых сессий (как во второй игре): старт/финиш + лидерборд.
  const minigame = useMinigameStore()

  const config = ref<GameConfig | null>(null)
  const balance = ref(0)
  const history = ref<HistoryItem[]>([])
  const screen = ref<Screen>('bet')
  const theme = ref<ThemeId>('red')
  const selectedBetId = ref<string | null>(null)
  const ready = ref(false)

  const round = ref<Round | null>(null)
  const phase = ref<Phase>('idle')
  const elapsedMs = ref(0)
  const baseMultiplier = ref(1)
  const currentLevels = ref(0)
  const cashoutInfo = ref<CashoutSuccess | null>(null)
  const result = ref<RoundResult | null>(null)
  const floaters = ref<Floater[]>([])
  const toast = ref<{ id: number; text: string } | null>(null)
  const showOnboarding = ref(false)

  let flightStart = 0
  let tickPending = false
  let cashoutPending = false
  let floaterSeq = 0
  let toastTimer: ReturnType<typeof setTimeout> | null = null
  // Бэкенд-сессия текущего раунда (для отправки счёта на сервер).
  let sessionId = ''
  let sessionSecret = ''

  const themeConfig = computed(() => config.value?.themes[theme.value] ?? null)
  const betOptions = computed<BetOption[]>(() => config.value?.betOptions ?? [])
  const selectedBet = computed(
    () => betOptions.value.find((o) => o.id === selectedBetId.value) ?? null,
  )
  const levelCount = computed(() => themeConfig.value?.levelThresholds.length ?? 0)

  /** Отображаемый (с учётом бустера) коэффициент — источник для HUD и рендера. */
  const displayMultiplier = computed(() => {
    if (!round.value || !themeConfig.value) return 1
    return displayedMultiplier(baseMultiplier.value, {
      boosterLevel: round.value.boosterLevel,
      boosterMultiplier: round.value.boosterMultiplier,
      thresholds: round.value.levels,
    })
  })

  const boosterActive = computed(() => {
    if (!round.value || round.value.boosterLevel === null) return false
    const th = round.value.levels[round.value.boosterLevel]
    return th !== undefined && baseMultiplier.value >= th
  })

  /** Живой предполагаемый выигрыш, если забрать прямо сейчас. */
  const liveWinnings = computed(() =>
    round.value ? Math.round(round.value.bet * displayMultiplier.value) : 0,
  )

  // Порог для кнопки «Забрать» — минимальный (от нуля): доступна сразу,
  // как только шар взлетел, а не только после прохождения 1-го уровня.
  const canCashout = computed(
    () => phase.value === 'flying' && cashoutInfo.value === null,
  )

  async function init() {
    config.value = await api.getConfig()
    balance.value = await api.getBalance()
    history.value = await api.getHistory()
    ready.value = true
  }

  async function refreshBalance() {
    balance.value = await api.getBalance()
  }

  async function refreshHistory() {
    history.value = await api.getHistory()
  }

  function setTheme(t: ThemeId) {
    theme.value = t
  }

  function selectBet(id: string) {
    const opt = betOptions.value.find((o) => o.id === id)
    if (!opt) return
    if (balance.value < opt.cost) {
      showToast('Не хватает бонусов')
      return
    }
    selectedBetId.value = id
  }

  function showToast(text: string) {
    toast.value = { id: Date.now(), text }
    if (toastTimer) clearTimeout(toastTimer)
    toastTimer = setTimeout(() => {
      toast.value = null
    }, 2200)
  }

  async function topUp(amount?: number) {
    balance.value = await api.topUp(amount)
    showToast('Баланс пополнен')
  }

  async function startRound(): Promise<boolean> {
    const opt = selectedBet.value
    if (!opt) return false
    if (balance.value < opt.cost) {
      showToast('Не хватает бонусов')
      return false
    }
    try {
      const r = await api.createRound(theme.value, opt.id)
      round.value = r
      balance.value = await api.getBalance()
      // Открываем игровую сессию на бэкенде (как во второй игре). Если бэкенд
      // недоступен — раунд всё равно играется на локальном движке.
      sessionId = ''
      sessionSecret = ''
      try {
        const s = await minigame.startSession()
        sessionId = s.session_id
        sessionSecret = s.secret
      } catch {
        /* играем локально, счёт на сервер не уйдёт */
      }
      phase.value = 'flying'
      elapsedMs.value = 0
      baseMultiplier.value = config.value?.startMultiplier ?? 1
      currentLevels.value = 0
      cashoutInfo.value = null
      result.value = null
      floaters.value = []
      screen.value = 'game'
      flightStart = performance.now()
      maybeOnboard()
      return true
    } catch (e) {
      if (e instanceof InsufficientBalanceError) showToast('Не хватает бонусов')
      else showToast('Ошибка запуска раунда')
      return false
    }
  }

  function maybeOnboard() {
    let seen = false
    try {
      seen = localStorage.getItem(ONBOARD_KEY) === '1'
    } catch {
      /* ignore */
    }
    if (!seen) {
      showOnboarding.value = true
      const dur = config.value?.onboardingDuration ?? 4000
      setTimeout(() => {
        showOnboarding.value = false
        try {
          localStorage.setItem(ONBOARD_KEY, '1')
        } catch {
          /* ignore */
        }
      }, dur)
    }
  }

  /** Кадр rAF из GameCanvas. now = performance.now(). */
  function tick(now: number) {
    if (phase.value !== 'flying' || !round.value || !config.value) return
    const el = now - flightStart
    elapsedMs.value = el
    // Локальная интерполяция кривой на 60 FPS (для плавности рендера)
    baseMultiplier.value = multiplierAt(el / 1000, config.value)

    // Пересечение уровней → очки-флоатеры
    const crossed = levelsCrossed(baseMultiplier.value, round.value.levels)
    if (crossed > currentLevels.value) {
      const gained = crossed - currentLevels.value
      currentLevels.value = crossed
      const pts = (config.value.pointsPerLine ?? 0) * gained
      pushFloater(`+${pts}`)
    }

    // Авторитетная проверка краха у «сервера» (без блокировки кадра)
    if (!tickPending) {
      tickPending = true
      const id = round.value.id
      api
        .resolveTick(id, el)
        .then((res) => {
          tickPending = false
          if (res.crashed && phase.value === 'flying' && round.value?.id === id) {
            onCrash()
          }
        })
        .catch(() => {
          tickPending = false
        })
    }
  }

  function pushFloater(text: string) {
    const id = ++floaterSeq
    floaters.value.push({ id, text })
    setTimeout(() => {
      floaters.value = floaters.value.filter((f) => f.id !== id)
    }, 1200)
  }

  /**
   * «Забрать»: фиксируем выигрыш, но шар ПРОДОЛЖАЕТ лететь до точки взрыва.
   * Раунд финализируется и уходит на экран результата уже в onCrash().
   */
  async function cashout() {
    if (!round.value || !canCashout.value || cashoutPending) return
    cashoutPending = true
    const id = round.value.id
    try {
      const res = await api.cashout(id, elapsedMs.value)
      if (!res.accepted) return
      cashoutInfo.value = res
      // phase остаётся 'flying' — tick() продолжает крутить кривую до краха
      balance.value = await api.getBalance()
      pushFloater(`+${res.winnings} бонусов`)
    } finally {
      cashoutPending = false
    }
  }

  async function onCrash() {
    phase.value = 'crashed'
    if (!round.value) return
    const id = round.value.id
    let bonus = 0
    try {
      const res = await api.finalizeRound(id)
      result.value = res
      bonus = res.puzzleBonus
      await refreshBalance()
      await refreshHistory()
      // Отправляем счёт раунда на бэкенд (очки за раунд) и обновляем лидерборд.
      if (sessionId) {
        try {
          await minigame.finishSession(sessionId, sessionSecret, res.points)
          await minigame.loadLeaderboard()
        } catch {
          /* сервер недоступен — счёт остаётся только локально */
        } finally {
          sessionId = ''
          sessionSecret = ''
        }
      }
    } catch {
      /* ignore */
    }
    // Даём проиграться анимации взрыва, затем экран результата
    setTimeout(() => {
      if (phase.value === 'crashed') screen.value = 'result'
      // Алерт о собранном пазле — после перехода на экран результата.
      if (bonus > 0) {
        window.alert(`Пазл собран! Вы получили все 4 фрагмента. Награда +${bonus} бонусов начислена.`)
      }
    }, 1100)
  }

  function playAgain() {
    resetRound()
    screen.value = 'bet'
  }

  async function repeatSame(): Promise<void> {
    const opt = selectedBet.value
    if (!opt || balance.value < opt.cost) {
      playAgain()
      showToast('Не хватает бонусов для повтора')
      return
    }
    resetRound()
    await startRound()
  }

  function resetRound() {
    round.value = null
    phase.value = 'idle'
    elapsedMs.value = 0
    baseMultiplier.value = 1
    currentLevels.value = 0
    cashoutInfo.value = null
    result.value = null
    floaters.value = []
  }

  return {
    config,
    balance,
    history,
    screen,
    theme,
    selectedBetId,
    ready,
    round,
    phase,
    elapsedMs,
    baseMultiplier,
    currentLevels,
    cashoutInfo,
    result,
    floaters,
    toast,
    showOnboarding,
    themeConfig,
    betOptions,
    selectedBet,
    levelCount,
    displayMultiplier,
    boosterActive,
    liveWinnings,
    canCashout,
    init,
    refreshBalance,
    refreshHistory,
    setTheme,
    selectBet,
    showToast,
    topUp,
    startRound,
    tick,
    cashout,
    playAgain,
    repeatSame,
    resetRound,
  }
})
