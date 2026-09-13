// Общие типы игры. Разделяются между UI, стором и слоем данных (GameApi).
// Никакой игровой логики здесь — только формы данных (DTO).

export type ThemeId = 'red' | 'green'

export interface ThemeConfig {
  id: ThemeId
  name: string
  /** Пороги коэффициента для каждого уровня (по возрастанию). Длина = кол-во уровней (red 12 / green 9). */
  levelThresholds: number[]
  /** Вероятность появления маркера бустера на каждом уровне (line_N_loot_prob). Длина = кол-во уровней. */
  lootProbabilities: number[]
}

/** Вариант ставки — «фрагмент пазла»: стоимость + множитель бустера. */
export interface BetOption {
  id: string
  /** Стоимость в бонусах. */
  cost: number
  /** Тир бустера: 1 = без бустера, 2/3/4 = множитель. */
  boosterTier: 1 | 2 | 3 | 4
  /** Значение множителя бустера (multiplier_tier_N_value). Для tier 1 = 1. */
  boosterMultiplier: number
}

export interface GameConfig {
  // Математическая модель
  alpha: number
  maxMultiplier: number
  minCrashMultiplier: number
  multiplierGrowthRate: number
  startMultiplier: number

  themes: Record<ThemeId, ThemeConfig>

  betOptions: BetOption[]

  // Очки
  pointsPerLine: number
  pointsCashoutBonus: number
  pointsBoosterBonus: number

  // Тайминги UI
  resultIdleTimeout: number
  onboardingDuration: number

  // Демо-кошелёк
  startingBalance: number
  topUpAmount: number
}

/**
 * Раунд, как его отдаёт «сервер» после создания.
 * ВАЖНО: crashPoint здесь НЕТ — UI не должен знать точку краха заранее.
 * Она приходит только в RoundResult (в момент/после краха).
 */
export interface Round {
  id: string
  theme: ThemeId
  bet: number
  boosterTier: 1 | 2 | 3 | 4
  boosterMultiplier: number
  /** Индекс уровня (0-based), на котором стоит маркер бустера. null = бустера в этом раунде нет. */
  boosterLevel: number | null
  /** Пороги уровней выбранной темы (копия для удобства рендера). */
  levels: number[]
  /** Плейсхолдер под provably-fair: SHA-256 от серверного seed. Полная проверка честности — позже. */
  hash: string
  /** Время старта раунда по «серверным» часам (мс, epoch). */
  startedAt: number
}

export interface CashoutSuccess {
  accepted: true
  /** Отображаемый (с учётом бустера) коэффициент выхода. */
  exitMultiplier: number
  /** Базовый коэффициент выхода (без бустера) — для отладки/честности. */
  baseExitMultiplier: number
  winnings: number
  boosterApplied: boolean
}

export interface CashoutTooLate {
  accepted: false
  reason: 'too_late' | 'already_cashed' | 'not_found'
}

export type CashoutResult = CashoutSuccess | CashoutTooLate

export interface TickResult {
  crashed: boolean
  /** Базовый коэффициент на данный момент (истина от «сервера»). */
  baseMultiplier: number
}

/** Итог завершённого раунда — источник для экрана результата и истории. */
export interface RoundResult {
  roundId: string
  theme: ThemeId
  bet: number
  win: boolean
  /** Коэффициент, на котором игрок забрал (displayed). null, если не успел. */
  exitMultiplier: number | null
  /** Коэффициент краха (базовый) — раскрывается только сейчас. */
  crashMultiplier: number
  boosterApplied: boolean
  boosterTier: 1 | 2 | 3 | 4
  winnings: number
  /** Сколько уровней пересёк шар до завершения. */
  levelsCrossed: number
  points: number
  /** Награда — индекс фрагмента пазла (0..3), выданного за раунд. */
  puzzlePiece: number
  /** Сколько уникальных фрагментов собрано после этого раунда (0..4). */
  puzzleCollected: number
  /** Бонус за собранный целиком пазл (0, если не собран в этом раунде). */
  puzzleBonus: number
}

export interface HistoryItem {
  id: string
  theme: ThemeId
  /** Коэффициент краха. */
  crashMultiplier: number
  /** Коэффициент выхода игрока (null = проигрыш / бот без cashout). */
  exitMultiplier: number | null
  win: boolean
  player: string
  /** true, если это раунд текущего игрока. */
  isSelf: boolean
  at: number
}
