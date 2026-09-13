import type { GameConfig, HistoryItem, ThemeId } from '~/shared/types/game'
import { generateCrashPoint, round2 } from '~/shared/lib/game/engine'
import type { Prng } from '~/shared/lib/game/prng'

const NICKNAMES = [
  'ШАРоман',
  'НебоЛётчик',
  'КапитанБриз',
  'Аэронавт77',
  'ОблакоJoe',
  'ВетерОк',
  'ПилотЛуна',
  'ЗакатХан',
  'СтратоКот',
  'ГондолаМастер',
  'ВысотаМакс',
  'ПерелётчикЪ',
]

/** Один фейковый раунд «другого игрока» для ленты истории. */
export function generateBotHistoryItem(prng: Prng, cfg: GameConfig, at: number): HistoryItem {
  const theme: ThemeId = prng.next() < 0.5 ? 'red' : 'green'
  const crash = generateCrashPoint(prng.nextOpen(), cfg)
  // Бот с вероятностью ~0.55 успевает забрать где-то до краха
  const cashedOut = prng.next() < 0.55 && crash > 1.1
  let exit: number | null = null
  if (cashedOut) {
    const frac = 0.3 + prng.next() * 0.6 // забрал между 30% и 90% пути до краха
    exit = round2(Math.max(1.05, 1 + (crash - 1) * frac))
  }
  const name = NICKNAMES[Math.floor(prng.next() * NICKNAMES.length)] ?? 'Игрок'
  return {
    id: `bot_${at}_${Math.floor(prng.next() * 1e6)}`,
    theme,
    crashMultiplier: crash,
    exitMultiplier: exit,
    win: exit !== null,
    player: name,
    isSelf: false,
    at,
  }
}

/** Начальная лента «Прошлые игры», чтобы экран ставки был живым. */
export function seedBotHistory(prng: Prng, cfg: GameConfig, count = 14): HistoryItem[] {
  const now = Date.now()
  const items: HistoryItem[] = []
  for (let i = 0; i < count; i++) {
    // Разносим по времени в прошлое
    items.push(generateBotHistoryItem(prng, cfg, now - (count - i) * 45_000))
  }
  return items
}
