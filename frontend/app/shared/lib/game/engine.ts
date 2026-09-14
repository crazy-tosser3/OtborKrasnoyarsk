import type { GameConfig } from '~/shared/types/game'

/**
 * Чистые функции математической модели. Никаких сайд-эффектов, DOM или таймеров —
 * чтобы их можно было 1:1 портировать на бэкенд и полностью покрыть Vitest.
 *
 * КЛЮЧЕВОЕ ПРАВИЛО БУСТЕРА:
 *   Точка краха задаётся и сравнивается ТОЛЬКО в БАЗОВОЙ шкале коэффициента
 *   (без бустера). Бустер влияет исключительно на ОТОБРАЖАЕМЫЙ коэффициент и
 *   на ВЫПЛАТУ. Так момент краха остаётся согласованным независимо от бустера,
 *   а игрок видит «скачок» (напр. 2.0 → 6.0 при ×3) на уровне бустера.
 */

/** Округление коэффициента до 2 знаков (для отображения/сравнений). */
export function round2(x: number): number {
  return Math.round(x * 100) / 100
}

/**
 * Генерация точки краха (в базовой шкале).
 * crash = min(maxMultiplier, minCrash / U^(1/alpha)), U ∈ (0, 1].
 * @param u равномерная величина из (0, 1]
 */
export function generateCrashPoint(
  u: number,
  cfg: Pick<GameConfig, 'alpha' | 'minCrashMultiplier' | 'maxMultiplier'>,
): number {
  const clampedU = Math.min(1, Math.max(1e-12, u))
  const raw = cfg.minCrashMultiplier / Math.pow(clampedU, 1 / cfg.alpha)
  const crash = Math.min(cfg.maxMultiplier, Math.max(cfg.minCrashMultiplier, raw))
  return round2(crash)
}

/** Коэффициент (базовый) в момент времени t (сек) от старта. */
export function multiplierAt(
  elapsedSec: number,
  cfg: Pick<GameConfig, 'startMultiplier' | 'multiplierGrowthRate'>,
): number {
  if (elapsedSec <= 0) return cfg.startMultiplier
  return cfg.startMultiplier * Math.exp(cfg.multiplierGrowthRate * elapsedSec)
}

/** Обратная функция: за сколько секунд базовый коэффициент достигнет m. */
export function timeForMultiplier(
  m: number,
  cfg: Pick<GameConfig, 'startMultiplier' | 'multiplierGrowthRate'>,
): number {
  if (m <= cfg.startMultiplier) return 0
  return Math.log(m / cfg.startMultiplier) / cfg.multiplierGrowthRate
}

/** Сколько порогов уровней (по возрастанию) достигнуто при данном базовом коэффициенте. */
export function levelsCrossed(baseMultiplier: number, thresholds: number[]): number {
  let n = 0
  for (const t of thresholds) {
    if (baseMultiplier >= t) n++
    else break
  }
  return n
}

/**
 * Выбор уровня бустера по весам (line_N_loot_prob). Веса нормализуются.
 * @param u равномерная величина [0, 1)
 * @returns индекс уровня (0-based) или null, если веса пусты/нулевые
 */
export function pickBoosterLevel(u: number, weights: number[]): number | null {
  const total = weights.reduce((s, w) => s + Math.max(0, w), 0)
  if (total <= 0) return null
  let acc = 0
  const target = u * total
  for (let i = 0; i < weights.length; i++) {
    acc += Math.max(0, weights[i]!)
    if (target < acc) return i
  }
  return weights.length - 1
}

/**
 * Отображаемый коэффициент с учётом бустера.
 * Если бустер есть и базовый коэффициент достиг порога уровня бустера — умножаем.
 */
export function displayedMultiplier(
  baseMultiplier: number,
  params: {
    boosterLevel: number | null
    boosterMultiplier: number
    thresholds: number[]
  },
): number {
  const { boosterLevel, boosterMultiplier, thresholds } = params
  if (boosterLevel === null || boosterMultiplier <= 1) return round2(baseMultiplier)
  const threshold = thresholds[boosterLevel]
  if (threshold !== undefined && baseMultiplier >= threshold) {
    return round2(baseMultiplier * boosterMultiplier)
  }
  return round2(baseMultiplier)
}

/** Сработал ли бустер к моменту, когда базовый коэффициент = baseMultiplier. */
export function boosterTriggered(
  baseMultiplier: number,
  boosterLevel: number | null,
  thresholds: number[],
): boolean {
  if (boosterLevel === null) return false
  const threshold = thresholds[boosterLevel]
  return threshold !== undefined && baseMultiplier >= threshold
}

/** Выигрыш = ставка × отображаемый коэффициент (целые бонусы). */
export function computeWinnings(bet: number, displayed: number): number {
  return Math.round(bet * displayed)
}

/**
 * Очки за раунд.
 * - за каждый пройденный уровень: pointsPerLine
 * - за успешный cashout: pointsCashoutBonus
 * - за сработавший бустер: pointsBoosterBonus
 * Очки начисляются и при проигрыше (за пройденные уровни), но без cashout-бонуса.
 */
export function computeRoundPoints(
  params: {
    levelsCrossed: number
    didCashout: boolean
    boosterApplied: boolean
  },
  cfg: Pick<GameConfig, 'pointsPerLine' | 'pointsCashoutBonus' | 'pointsBoosterBonus'>,
): number {
  let pts = params.levelsCrossed * cfg.pointsPerLine
  if (params.didCashout) pts += cfg.pointsCashoutBonus
  if (params.boosterApplied) pts += cfg.pointsBoosterBonus
  return pts
}
