import type { GameConfig } from '~/shared/types/game'

/**
 * ЕДИНСТВЕННЫЙ источник числовых параметров игры.
 * Компоненты и стор читают конфиг ТОЛЬКО через GameApi.getConfig() — никаких
 * магических констант в UI. Позже эти же параметры будет отдавать бэкенд/админка.
 */
export const DEFAULT_CONFIG: GameConfig = {
  // --- Математическая модель ---
  // P(crash > x) = (minCrash / x)^alpha; crash = min(max, minCrash / U^(1/alpha))
  alpha: 1.15,
  // Шар лопается в диапазоне x1.4–x4.0.
  maxMultiplier: 4,
  minCrashMultiplier: 1.4,
  // m(t) = startMultiplier * e^(growthRate * t), t в секундах
  multiplierGrowthRate: 0.16,
  startMultiplier: 1.0,

  themes: {
    red: {
      id: 'red',
      name: 'Красный шар',
      // 12 уровней, равномерно в диапазоне краха x1.4–x4.0
      levelThresholds: [1.4, 1.64, 1.87, 2.11, 2.35, 2.58, 2.82, 3.05, 3.29, 3.53, 3.76, 4.0],
      // Вес появления маркера бустера на уровне (нормализуется при выборе)
      lootProbabilities: [0.2, 0.18, 0.16, 0.12, 0.1, 0.08, 0.06, 0.04, 0.03, 0.015, 0.01, 0.005],
    },
    green: {
      id: 'green',
      name: 'Зелёный шар',
      // 9 уровней в пределах x1–x4 (первые три оставлены как есть)
      levelThresholds: [1.2, 1.5, 1.8, 2.2, 2.5, 2.9, 3.3, 3.7, 4.0],
      lootProbabilities: [0.22, 0.2, 0.16, 0.13, 0.1, 0.08, 0.06, 0.03, 0.02],
    },
  },

  // 4 фрагмента пазла: стоимость + множитель бустера
  betOptions: [
    { id: 'f1', cost: 50, boosterTier: 1, boosterMultiplier: 1 },
    { id: 'f2', cost: 100, boosterTier: 2, boosterMultiplier: 2 },
    { id: 'f3', cost: 150, boosterTier: 3, boosterMultiplier: 3 },
    { id: 'f4', cost: 200, boosterTier: 4, boosterMultiplier: 4 },
  ],

  // --- Очки ---
  pointsPerLine: 10,
  pointsCashoutBonus: 50,
  pointsBoosterBonus: 100,

  // --- Тайминги UI (мс) ---
  resultIdleTimeout: 10_000,
  onboardingDuration: 4_000,

  // --- Демо-кошелёк ---
  startingBalance: 1000,
  topUpAmount: 500,
}
