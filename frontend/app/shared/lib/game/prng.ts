/**
 * Детерминированный PRNG. Одинаковый seed → одинаковая последовательность.
 * Используется mock-«сервером» для воспроизводимых демо (NUXT_PUBLIC_DEV_FIXED_SEED)
 * и для тестов. Позже эту роль возьмёт серверный seed (provably-fair).
 */

/** Хеширует строковый seed в 32-битное число (xmur3). */
export function hashSeed(str: string): number {
  let h = 1779033703 ^ str.length
  for (let i = 0; i < str.length; i++) {
    h = Math.imul(h ^ str.charCodeAt(i), 3432918353)
    h = (h << 13) | (h >>> 19)
  }
  h = Math.imul(h ^ (h >>> 16), 2246822507)
  h = Math.imul(h ^ (h >>> 13), 3266489909)
  return (h ^= h >>> 16) >>> 0
}

export interface Prng {
  /** Следующее число в [0, 1). */
  next(): number
  /** Число в (0, 1] — удобно для формулы краха (нельзя делить на 0). */
  nextOpen(): number
}

/** mulberry32 — быстрый, детерминированный. */
export function createPrng(seed: string | number): Prng {
  let a = typeof seed === 'number' ? seed >>> 0 : hashSeed(seed)
  const next = (): number => {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
  return {
    next,
    nextOpen: () => {
      // Сдвигаем [0,1) в (0,1]
      return 1 - next()
    },
  }
}
