import type {
  CashoutResult,
  GameConfig,
  HistoryItem,
  Round,
  RoundResult,
  ThemeId,
} from '~/shared/types/game'
import { DEFAULT_CONFIG } from '~/shared/config/game.config'
import type { GameApi, PuzzleState, PuzzleAward } from '~/shared/api/GameApi'
import { InsufficientBalanceError } from '~/shared/api/GameApi'
import {
  boosterTriggered,
  computeRoundPoints,
  computeWinnings,
  displayedMultiplier,
  generateCrashPoint,
  levelsCrossed,
  multiplierAt,
  pickBoosterLevel,
  round2,
  timeForMultiplier,
} from '~/shared/lib/game/engine'
import { createPrng, hashSeed, type Prng } from '~/shared/lib/game/prng'
import { generateBotHistoryItem, seedBotHistory } from './bots'

const PUZZLE_BONUS = 500 // бонус за собранные 4 фрагмента

interface ActiveRound {
  round: Round
  cfg: GameConfig
  crashPoint: number // базовый коэффициент краха (скрыт от UI)
  crashTimeMs: number
  thresholds: number[]
  cashout: {
    baseMultiplier: number
    displayed: number
    winnings: number
    boosterApplied: boolean
  } | null
  finalized: RoundResult | null
}

/** Простой интерфейс хранилища — localStorage в браузере, in-memory в тестах/SSR. */
interface KV {
  get(key: string): string | null
  set(key: string, value: string): void
}

function memoryKV(): KV {
  const m = new Map<string, string>()
  return {
    get: (k) => (m.has(k) ? m.get(k)! : null),
    set: (k, v) => void m.set(k, v),
  }
}

const KEY = {
  balance: 'balloon:balance',
  history: 'balloon:history',
  pieces: 'balloon:pieces',
  points: 'balloon:points',
  counter: 'balloon:counter',
  seedInit: 'balloon:seedInit',
}

export interface MockGameApiOptions {
  /** Фиксированный seed для воспроизводимости (NUXT_PUBLIC_DEV_FIXED_SEED). */
  fixedSeed?: string
  config?: GameConfig
  storage?: KV | null
  /** Искусственная сетевая задержка (мс). По умолчанию 50–150. 0 = без задержки (тесты). */
  latency?: [number, number] | 0
}

/**
 * «Сервер в браузере». Полностью имитирует поведение будущего бэкенда:
 * авторитарно генерирует крах/бустер, хранит баланс и историю в localStorage,
 * подмешивает ботов. UI получает crashPoint только после краха.
 */
export class MockGameApi implements GameApi {
  private cfg: GameConfig
  private kv: KV
  private baseSeed: string
  private latency: [number, number] | 0
  private rounds = new Map<string, ActiveRound>()
  private finalizedResults = new Map<string, RoundResult>()

  constructor(opts: MockGameApiOptions = {}) {
    this.cfg = opts.config ?? DEFAULT_CONFIG
    this.kv = opts.storage ?? detectStorage()
    this.latency = opts.latency ?? [50, 150]
    // Базовый seed: фиксированный из env для воспроизводимости, иначе случайный,
    // но стабильный в пределах установки (persist), чтобы счётчик раундов имел смысл.
    if (opts.fixedSeed) {
      this.baseSeed = opts.fixedSeed
    } else {
      let s = this.kv.get(KEY.seedInit)
      if (!s) {
        s = `${Date.now()}_${Math.floor(Math.random() * 1e9)}`
        this.kv.set(KEY.seedInit, s)
      }
      this.baseSeed = s
    }
    this.ensureSeeded()
  }

  // ---- служебное ----

  private async delay(): Promise<void> {
    if (this.latency === 0) return
    const [min, max] = this.latency
    const ms = min + Math.random() * (max - min)
    await new Promise((r) => setTimeout(r, ms))
  }

  private num(key: string, fallback: number): number {
    const v = this.kv.get(key)
    const n = v == null ? NaN : Number(v)
    return Number.isFinite(n) ? n : fallback
  }

  private setNum(key: string, v: number): void {
    this.kv.set(key, String(v))
  }

  private ensureSeeded(): void {
    if (this.kv.get(KEY.balance) == null) this.setNum(KEY.balance, this.cfg.startingBalance)
    if (this.kv.get(KEY.history) == null) {
      const prng = createPrng(`${this.baseSeed}:bots`)
      this.kv.set(KEY.history, JSON.stringify(seedBotHistory(prng, this.cfg)))
    }
    if (this.kv.get(KEY.pieces) == null) this.kv.set(KEY.pieces, JSON.stringify([]))
    if (this.kv.get(KEY.points) == null) this.setNum(KEY.points, 0)
    if (this.kv.get(KEY.counter) == null) this.setNum(KEY.counter, 0)
  }

  private readHistory(): HistoryItem[] {
    try {
      return JSON.parse(this.kv.get(KEY.history) ?? '[]') as HistoryItem[]
    } catch {
      return []
    }
  }

  private writeHistory(items: HistoryItem[]): void {
    this.kv.set(KEY.history, JSON.stringify(items.slice(0, 60)))
  }

  private readPieces(): number[] {
    try {
      return JSON.parse(this.kv.get(KEY.pieces) ?? '[]') as number[]
    } catch {
      return []
    }
  }

  /** Начисляет один фрагмент пазла (без сетевой задержки). Общая логика награды. */
  private grantPuzzlePiece(): PuzzleAward {
    const owned = this.readPieces()
    const piece = owned.length % 4 // следующий фрагмент по порядку 0→3
    let pieces = [...owned, piece]
    let bonus = 0
    let completed = false
    if (pieces.length >= 4) {
      completed = true
      bonus = PUZZLE_BONUS
      this.setNum(KEY.balance, this.num(KEY.balance, 0) + bonus)
      pieces = [] // пазл собран — начинаем новый
    }
    this.kv.set(KEY.pieces, JSON.stringify(pieces))
    return { piece, collected: pieces.length, total: 4, completed, bonus }
  }

  // ---- GameApi ----

  async getConfig(): Promise<GameConfig> {
    await this.delay()
    // Клонируем, чтобы UI не мутировал источник
    return structuredClone(this.cfg)
  }

  async getBalance(): Promise<number> {
    await this.delay()
    return this.num(KEY.balance, this.cfg.startingBalance)
  }

  async topUp(amount = this.cfg.topUpAmount): Promise<number> {
    await this.delay()
    const next = this.num(KEY.balance, 0) + amount
    this.setNum(KEY.balance, next)
    return next
  }

  async getHistory(): Promise<HistoryItem[]> {
    await this.delay()
    return this.readHistory()
  }

  async getPuzzle(): Promise<PuzzleState> {
    await this.delay()
    return { collected: this.readPieces().length, total: 4 }
  }

  async awardPuzzlePiece(): Promise<PuzzleAward> {
    await this.delay()
    return this.grantPuzzlePiece()
  }

  async createRound(theme: ThemeId, betOptionId: string): Promise<Round> {
    await this.delay()
    const opt = this.cfg.betOptions.find((o) => o.id === betOptionId)
    if (!opt) throw new Error(`Unknown bet option: ${betOptionId}`)

    const balance = this.num(KEY.balance, 0)
    if (balance < opt.cost) throw new InsufficientBalanceError()

    // Списываем ставку
    this.setNum(KEY.balance, balance - opt.cost)

    // Сдвигаем счётчик раундов и делаем детерминированный per-round seed
    const counter = this.num(KEY.counter, 0) + 1
    this.setNum(KEY.counter, counter)
    const seed = `${this.baseSeed}:round:${counter}`
    const prng: Prng = createPrng(seed)

    const themeCfg = this.cfg.themes[theme]
    const crashPoint = generateCrashPoint(prng.nextOpen(), this.cfg)

    let boosterLevel: number | null = null
    if (opt.boosterTier > 1) {
      boosterLevel = pickBoosterLevel(prng.next(), themeCfg.lootProbabilities)
    }

    const crashTimeMs = timeForMultiplier(crashPoint, this.cfg) * 1000
    const startedAt = Date.now()
    const id = `r_${counter}_${startedAt}`
    // Плейсхолдер под provably-fair (полноценный SHA-256 через Web Crypto — позже)
    const hash = hashSeed(seed).toString(16).padStart(8, '0')

    const round: Round = {
      id,
      theme,
      bet: opt.cost,
      boosterTier: opt.boosterTier,
      boosterMultiplier: opt.boosterMultiplier,
      boosterLevel,
      levels: [...themeCfg.levelThresholds],
      hash,
      startedAt,
    }

    this.rounds.set(id, {
      round,
      cfg: this.cfg,
      crashPoint,
      crashTimeMs,
      thresholds: themeCfg.levelThresholds,
      cashout: null,
      finalized: null,
    })

    return structuredClone(round)
  }

  async resolveTick(roundId: string, elapsedMs: number) {
    // Тик realtime-потока: намеренно почти без задержки, чтобы держать 60 FPS.
    const rec = this.rounds.get(roundId)
    if (!rec) return { crashed: true, baseMultiplier: 1 }
    const crashed = elapsedMs >= rec.crashTimeMs
    const base = crashed
      ? rec.crashPoint
      : round2(Math.min(rec.crashPoint, multiplierAt(elapsedMs / 1000, rec.cfg)))
    return { crashed, baseMultiplier: base }
  }

  async cashout(roundId: string, elapsedMs: number): Promise<CashoutResult> {
    await this.delay()
    const rec = this.rounds.get(roundId)
    if (!rec) return { accepted: false, reason: 'not_found' }
    if (rec.cashout) return { accepted: false, reason: 'already_cashed' }
    if (elapsedMs >= rec.crashTimeMs) return { accepted: false, reason: 'too_late' }

    const base = round2(Math.min(rec.crashPoint, multiplierAt(elapsedMs / 1000, rec.cfg)))
    const displayed = displayedMultiplier(base, {
      boosterLevel: rec.round.boosterLevel,
      boosterMultiplier: rec.round.boosterMultiplier,
      thresholds: rec.thresholds,
    })
    const applied = boosterTriggered(base, rec.round.boosterLevel, rec.thresholds)
    const winnings = computeWinnings(rec.round.bet, displayed)

    rec.cashout = { baseMultiplier: base, displayed, winnings, boosterApplied: applied }
    this.setNum(KEY.balance, this.num(KEY.balance, 0) + winnings)

    return {
      accepted: true,
      exitMultiplier: displayed,
      baseExitMultiplier: base,
      winnings,
      boosterApplied: applied,
    }
  }

  async finalizeRound(roundId: string): Promise<RoundResult> {
    await this.delay()
    const cached = this.finalizedResults.get(roundId)
    if (cached) return structuredClone(cached)
    const rec = this.rounds.get(roundId)
    if (!rec) throw new Error(`Round not found: ${roundId}`)

    const { round, crashPoint, thresholds } = rec
    const win = rec.cashout !== null
    const resolutionBase = win ? rec.cashout!.baseMultiplier : crashPoint
    const crossed = levelsCrossed(resolutionBase, thresholds)
    // Бустер даёт очки, только если реально применился к выплате (т.е. был cashout после уровня)
    const boosterForPoints = win ? rec.cashout!.boosterApplied : false
    const points = computeRoundPoints(
      { levelsCrossed: crossed, didCashout: win, boosterApplied: boosterForPoints },
      rec.cfg,
    )

    // Награда: ровно ОДИН фрагмент пазла за раунд.
    const award = this.grantPuzzlePiece()

    // Накопительные очки (для будущего рейтинга)
    this.setNum(KEY.points, this.num(KEY.points, 0) + points)

    const result: RoundResult = {
      roundId: round.id,
      theme: round.theme,
      bet: round.bet,
      win,
      exitMultiplier: win ? rec.cashout!.displayed : null,
      crashMultiplier: crashPoint,
      boosterApplied: boosterForPoints,
      boosterTier: round.boosterTier,
      winnings: win ? rec.cashout!.winnings : 0,
      levelsCrossed: crossed,
      points,
      puzzlePiece: award.piece,
      puzzleCollected: award.collected,
      puzzleBonus: award.bonus,
    }
    rec.finalized = result
    this.finalizedResults.set(roundId, result)

    // Запись в историю
    const history = this.readHistory()
    history.unshift({
      id: round.id,
      theme: round.theme,
      crashMultiplier: crashPoint,
      exitMultiplier: win ? rec.cashout!.displayed : null,
      win,
      player: 'Вы',
      isSelf: true,
      at: Date.now(),
    })
    // Иногда подмешиваем бота, чтобы лента жила
    const botPrng = createPrng(`${round.id}:bot`)
    if (botPrng.next() < 0.7) {
      history.unshift(generateBotHistoryItem(botPrng, rec.cfg, Date.now() - 1000))
    }
    this.writeHistory(history)

    // Раунд больше не активен (восстановление после перезагрузки — этап позже)
    this.rounds.delete(roundId)
    return structuredClone(result)
  }
}

function detectStorage(): KV {
  if (typeof window !== 'undefined' && window.localStorage) {
    const ls = window.localStorage
    return {
      get: (k) => {
        try {
          return ls.getItem(k)
        } catch {
          return null
        }
      },
      set: (k, v) => {
        try {
          ls.setItem(k, v)
        } catch {
          /* приватный режим / переполнение — молча игнорируем */
        }
      },
    }
  }
  return memoryKV()
}
