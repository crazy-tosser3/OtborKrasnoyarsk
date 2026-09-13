import type {
  CashoutResult,
  GameConfig,
  HistoryItem,
  Round,
  RoundResult,
  ThemeId,
  TickResult,
} from '~/shared/types/game'

/**
 * Контракт слоя данных. UI и стор зависят ТОЛЬКО от этого интерфейса и никогда
 * не считают исход раунда сами. Сейчас реализация — MockGameApi («сервер в
 * браузере»). Позже её заменит HttpGameApi/WsRealtimeChannel без правок UI.
 *
 * Модель авторитарна к «серверу»: crashPoint не отдаётся при создании раунда,
 * крах и выплату подтверждает реализация API (resolveTick / cashout).
 */
export interface GameApi {
  getConfig(): Promise<GameConfig>
  getBalance(): Promise<number>
  topUp(amount?: number): Promise<number>
  getHistory(): Promise<HistoryItem[]>

  /** Создаёт раунд и списывает ставку. Бросает, если баланса не хватает. */
  createRound(theme: ThemeId, betOptionId: string): Promise<Round>

  /**
   * Спрашивает у «сервера», лопнул ли шар к моменту elapsedMs.
   * Возвращает базовый коэффициент — истина о состоянии полёта.
   */
  resolveTick(roundId: string, elapsedMs: number): Promise<TickResult>

  /** Попытка забрать выигрыш на момент elapsedMs. */
  cashout(roundId: string, elapsedMs: number): Promise<CashoutResult>

  /**
   * Финализирует раунд (после краха) и возвращает итог: очки, награду, коэф. краха.
   * Пишет раунд в историю. Идемпотентна по roundId.
   */
  finalizeRound(roundId: string): Promise<RoundResult>

  /** Текущий прогресс сбора пазла: сколько фрагментов из 4 собрано. */
  getPuzzle(): Promise<PuzzleState>

  /**
   * Выдаёт РОВНО ОДИН фрагмент пазла (за сыгранную игру). Когда собрано 4 —
   * начисляет награду и сбрасывает коллекцию (completed=true, bonus>0).
   */
  awardPuzzlePiece(): Promise<PuzzleAward>
}

export interface PuzzleState {
  collected: number
  total: number
}

export interface PuzzleAward extends PuzzleState {
  piece: number
  completed: boolean
  bonus: number
}

export class InsufficientBalanceError extends Error {
  constructor() {
    super('Не хватает бонусов')
    this.name = 'InsufficientBalanceError'
  }
}
