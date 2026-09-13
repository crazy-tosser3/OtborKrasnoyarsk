<script setup lang="ts">
// Мини-игра «Ловец пазлов»: лови фрагменты и золото, избегай бомб.
// Логика раунда живёт здесь (тесно связана с канвасом), а работа с бэкендом —
// через minigame-стор (start/finish/leaderboard) в общем стиле проекта.
import { useMinigameStore } from '~/stores/minigame'
import { useAuthStore } from '~/stores/auth'

const minigame = useMinigameStore()
const auth = useAuthStore()

// ── Типы падающих фигур (монохром + жёлтый акцент, красный только для бомбы) ──
const TYPES: Record<string, { color: string; points: number }> = {
  piece: { color: '#9a958c', points: 10 },
  gold: { color: '#f6c445', points: 50 },
  booster: { color: '#151515', points: 100 },
  bomb: { color: '#e5484d', points: 0 },
}

const canvasRef = ref<HTMLCanvasElement | null>(null)

// Реактивное состояние для HUD и оверлея.
const score = ref(0)
const lives = ref(3)
const timeLeft = ref(0)
const combo = ref(0)
const bestCombo = ref(0)
const gameOver = ref(false)
const finishStatus = ref('')
const starting = ref(false)

// Итог с сервера (после «подкрутки»): очки с учётом множителя/лимита,
// выданная награда и бонус за серию. null — пока раунд не завершён.
const finalScore = ref<number | null>(null)
const rewardCreated = ref(false)
const bonusGranted = ref(false)

// Внутреннее состояние цикла (не реактивное — меняется каждый кадр).
let GAME_DURATION = 30
let running = false
let shake = 0
let spawnAccumulator = 0
let lastFrameTime = 0
let timer: ReturnType<typeof setInterval> | null = null
let sessionId = ''
let sessionSecret = ''
const items: any[] = []
const particles: any[] = []
const catcher = { x: 500, y: 300, radius: 34, active: false }

const difficulty = () => Math.min(Math.max((GAME_DURATION - timeLeft.value) / GAME_DURATION, 0), 1)

// ── Ввод ──
const canvasPoint = (clientX: number, clientY: number) => {
  const c = canvasRef.value!
  const rect = c.getBoundingClientRect()
  return {
    x: (clientX - rect.left) * (c.width / rect.width),
    y: (clientY - rect.top) * (c.height / rect.height),
  }
}
const onMouseMove = (e: MouseEvent) => {
  const p = canvasPoint(e.clientX, e.clientY)
  catcher.x = p.x
  catcher.y = p.y
  catcher.active = true
}
const onMouseLeave = () => {
  catcher.active = false
}
const onTouchMove = (e: TouchEvent) => {
  e.preventDefault()
  const t = e.touches[0]!
  const p = canvasPoint(t.clientX, t.clientY)
  catcher.x = p.x
  catcher.y = p.y - 60
  catcher.active = true
}

// ── Спавн ──
const randomType = (t: number) => {
  const r = Math.random()
  const pieceCut = 0.55 - t * 0.16
  const goldCut = pieceCut + (0.27 - t * 0.06)
  const boosterCut = goldCut + (0.12 + t * 0.05)
  if (r < pieceCut) return 'piece'
  if (r < goldCut) return 'gold'
  if (r < boosterCut) return 'booster'
  return 'bomb'
}
const spawnInterval = (t: number) => 640 - t * 380
const spawnItem = (t: number) => {
  const c = canvasRef.value!
  const speedBoost = 1 + t * 1.2
  items.push({
    x: 40 + Math.random() * (c.width - 80),
    y: -40,
    size: 32 - t * 6,
    speed: (2.2 + Math.random() * 3.6) * speedBoost,
    rot: Math.random() * Math.PI,
    spin: (Math.random() - 0.5) * (0.08 + t * 0.06),
    age: 0,
    wobbleAmp: t * (1.2 + Math.random() * 2.4),
    wobbleSpeed: 0.03 + Math.random() * 0.05,
    wobblePhase: Math.random() * Math.PI * 2,
    type: randomType(t),
  })
}
const spawnParticles = (x: number, y: number, color: string, n: number) => {
  for (let i = 0; i < n; i++) {
    const angle = Math.random() * Math.PI * 2
    const speed = 1.5 + Math.random() * 3.5
    particles.push({ x, y, vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed, life: 1, color })
  }
}

// ── Отрисовка ──
const puzzlePath = (ctx: CanvasRenderingContext2D, size: number) => {
  const s = size
  const k = s * 0.28
  ctx.beginPath()
  ctx.moveTo(-s / 2, -s / 2)
  ctx.lineTo(-k / 2, -s / 2)
  ctx.arc(0, -s / 2, k / 2, Math.PI, 0, true)
  ctx.lineTo(s / 2, -s / 2)
  ctx.lineTo(s / 2, s / 2)
  ctx.lineTo(k / 2, s / 2)
  ctx.arc(0, s / 2, k / 2, 0, Math.PI, true)
  ctx.lineTo(-s / 2, s / 2)
  ctx.closePath()
}
const drawGrid = (ctx: CanvasRenderingContext2D, c: HTMLCanvasElement) => {
  ctx.save()
  ctx.strokeStyle = '#00000010'
  ctx.lineWidth = 1
  for (let x = 0; x < c.width; x += 50) {
    ctx.beginPath()
    ctx.moveTo(x, 0)
    ctx.lineTo(x, c.height)
    ctx.stroke()
  }
  for (let y = 0; y < c.height; y += 50) {
    ctx.beginPath()
    ctx.moveTo(0, y)
    ctx.lineTo(c.width, y)
    ctx.stroke()
  }
  ctx.restore()
}
const drawItems = (ctx: CanvasRenderingContext2D) => {
  for (const item of items) {
    const def = TYPES[item.type]!
    ctx.save()
    ctx.translate(item.x, item.y)
    ctx.rotate(item.rot)
    ctx.fillStyle = def.color
    if (item.type === 'bomb') {
      ctx.beginPath()
      for (let i = 0; i < 8; i++) {
        const a = (Math.PI / 4) * i
        const r = i % 2 === 0 ? item.size / 2 : item.size / 3.2
        const px = Math.cos(a) * r
        const py = Math.sin(a) * r
        if (i === 0) ctx.moveTo(px, py)
        else ctx.lineTo(px, py)
      }
      ctx.closePath()
      ctx.fill()
    } else {
      puzzlePath(ctx, item.size)
      ctx.fill()
      ctx.fillStyle = '#ffffff55'
      ctx.beginPath()
      ctx.ellipse(-item.size * 0.15, -item.size * 0.2, item.size * 0.18, item.size * 0.1, -0.5, 0, Math.PI * 2)
      ctx.fill()
    }
    ctx.restore()
  }
}
const drawParticles = (ctx: CanvasRenderingContext2D) => {
  for (const p of particles) {
    ctx.save()
    ctx.globalAlpha = Math.max(p.life, 0)
    ctx.fillStyle = p.color
    ctx.beginPath()
    ctx.arc(p.x, p.y, 3, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}
const drawCatcher = (ctx: CanvasRenderingContext2D) => {
  if (!catcher.active) return
  ctx.save()
  ctx.translate(catcher.x, catcher.y)
  const pulse = 1 + Math.sin(Date.now() / 160) * 0.04
  ctx.strokeStyle = '#151515'
  ctx.lineWidth = 3
  ctx.beginPath()
  ctx.arc(0, 0, catcher.radius * pulse, 0, Math.PI * 2)
  ctx.stroke()
  ctx.strokeStyle = '#f6c445'
  ctx.lineWidth = 1.5
  ctx.beginPath()
  ctx.arc(0, 0, catcher.radius * pulse + 8, 0, Math.PI * 2)
  ctx.stroke()
  ctx.fillStyle = '#f6c445'
  ctx.beginPath()
  ctx.arc(0, 0, 3, 0, Math.PI * 2)
  ctx.fill()
  ctx.restore()
}

const updateParticles = () => {
  for (let i = particles.length - 1; i >= 0; i--) {
    const p = particles[i]
    p.x += p.vx
    p.y += p.vy
    p.vy += 0.12
    p.life -= 0.035
    if (p.life <= 0) particles.splice(i, 1)
  }
}
const updateItems = (c: HTMLCanvasElement) => {
  for (let i = items.length - 1; i >= 0; i--) {
    const item = items[i]
    item.y += item.speed
    item.rot += item.spin
    item.age += 1
    item.x += Math.sin(item.age * item.wobbleSpeed + item.wobblePhase) * item.wobbleAmp * 0.06

    const dx = item.x - catcher.x
    const dy = item.y - catcher.y
    const caught = catcher.active && Math.sqrt(dx * dx + dy * dy) < catcher.radius + item.size / 2

    if (caught) {
      if (item.type === 'bomb') {
        lives.value--
        combo.value = 0
        shake = 10
        spawnParticles(item.x, item.y, TYPES.bomb!.color, 18)
      } else {
        score.value += TYPES[item.type]!.points
        combo.value++
        bestCombo.value = Math.max(bestCombo.value, combo.value)
        spawnParticles(item.x, item.y, TYPES[item.type]!.color, 12)
      }
      items.splice(i, 1)
      continue
    }
    if (item.y - item.size / 2 > c.height) {
      if (item.type !== 'bomb') {
        lives.value--
        combo.value = 0
        shake = 5
      }
      items.splice(i, 1)
    }
  }
}

const render = () => {
  if (!running) return
  const c = canvasRef.value
  const ctx = c?.getContext('2d')
  if (!c || !ctx) return

  const now = performance.now()
  const dt = now - lastFrameTime
  lastFrameTime = now

  const t = difficulty()
  catcher.radius = 34 - t * 9

  spawnAccumulator += dt
  if (spawnAccumulator > spawnInterval(t)) {
    spawnAccumulator = 0
    spawnItem(t)
  }

  ctx.save()
  if (shake > 0) {
    ctx.translate((Math.random() - 0.5) * shake, (Math.random() - 0.5) * shake)
    shake *= 0.85
    if (shake < 0.5) shake = 0
  }
  ctx.clearRect(-20, -20, c.width + 40, c.height + 40)
  drawGrid(ctx, c)
  updateItems(c)
  updateParticles()
  drawParticles(ctx)
  drawItems(ctx)
  drawCatcher(ctx)
  ctx.restore()

  if (lives.value <= 0) {
    finishGame()
    return
  }
  requestAnimationFrame(render)
}

// ── Жизненный цикл раунда ──
const beginRound = (duration: number) => {
  GAME_DURATION = duration || 30
  timeLeft.value = GAME_DURATION
  score.value = 0
  lives.value = 3
  combo.value = 0
  bestCombo.value = 0
  shake = 0
  items.length = 0
  particles.length = 0
  spawnAccumulator = 0
  lastFrameTime = performance.now()
  gameOver.value = false

  running = true
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) finishGame()
  }, 1000)
  render()
}

const gameApi = useGameApi()

const finishGame = async () => {
  if (gameOver.value) return // защита от повторного вызова (таймер + жизни=0)
  running = false
  if (timer) clearInterval(timer)
  gameOver.value = true
  finalScore.value = null
  rewardCreated.value = false
  bonusGranted.value = false
  finishStatus.value = 'Сохраняем результат…'
  try {
    // Сервер применяет админскую «подкрутку» и возвращает итог: очки после
    // множителя/лимита, факт выдачи награды и бонуса за серию.
    const res = await minigame.finishSession(sessionId, sessionSecret, score.value)
    finalScore.value = res.score
    rewardCreated.value = res.reward_created
    bonusGranted.value = res.bonus_granted
    finishStatus.value = 'Результат сохранён'
  } catch (err) {
    finishStatus.value = 'Не удалось сохранить: ' + (err instanceof Error ? err.message : '')
  }
  // Награда за сыгранную игру — один фрагмент пазла.
  try {
    const award = await gameApi.awardPuzzlePiece()
    if (award.completed) {
      window.alert(`Пазл собран! Вы получили все 4 фрагмента. Награда +${award.bonus} бонусов начислена.`)
    }
  } catch {
    /* награда-пазл не критична */
  }
  await minigame.loadLeaderboard()
}

const startNewSession = async () => {
  starting.value = true
  try {
    const session = await minigame.startSession()
    sessionId = session.session_id
    sessionSecret = session.secret
    beginRound(session.duration)
  } catch (err) {
    finishStatus.value = 'Не удалось начать сессию: ' + (err instanceof Error ? err.message : '')
    gameOver.value = true
  } finally {
    starting.value = false
  }
}

const sortedLeaderboard = computed(() =>
  [...minigame.leaderboard].sort((a, b) => b.score - a.score).slice(0, 10),
)

onMounted(() => {
  const c = canvasRef.value!
  c.addEventListener('mousemove', onMouseMove)
  c.addEventListener('mouseleave', onMouseLeave)
  c.addEventListener('touchmove', onTouchMove, { passive: false })
  startNewSession()
})
onBeforeUnmount(() => {
  running = false
  if (timer) clearInterval(timer)
  const c = canvasRef.value
  if (c) {
    c.removeEventListener('mousemove', onMouseMove)
    c.removeEventListener('mouseleave', onMouseLeave)
    c.removeEventListener('touchmove', onTouchMove)
  }
})
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="mx-auto flex w-full max-w-5xl flex-col gap-4 px-4 py-6">
      <div class="flex items-center justify-between">
        <button
          class="cursor-pointer text-sm text-muted transition-colors hover:text-ink"
          data-testid="puzzle-back"
          @click="navigateTo('/')"
        >
          ← На главную
        </button>
        <h1 class="text-2xl font-extrabold">Ловец пазлов</h1>
      </div>

      <!-- HUD -->
      <div class="grid grid-cols-4 gap-3">
        <div class="card px-4 py-3">
          <div class="text-xs text-muted">Счёт</div>
          <div class="text-2xl font-extrabold text-accent-deep">{{ score }}</div>
        </div>
        <div class="card px-4 py-3">
          <div class="text-xs text-muted">Жизни</div>
          <div class="text-2xl font-extrabold">{{ Math.max(lives, 0) }}</div>
        </div>
        <div class="card px-4 py-3">
          <div class="text-xs text-muted">Время</div>
          <div class="text-2xl font-extrabold">{{ timeLeft }}</div>
        </div>
        <div class="card px-4 py-3">
          <div class="text-xs text-muted">Комбо</div>
          <div class="text-2xl font-extrabold">{{ combo }}</div>
        </div>
      </div>

      <!-- Игровое поле -->
      <div class="relative overflow-hidden rounded-card border border-line bg-surface">
        <canvas ref="canvasRef" width="1000" height="600" class="block h-auto w-full cursor-none" />

        <!-- Оверлей завершения -->
        <div
          v-if="gameOver"
          class="absolute inset-0 flex items-center justify-center bg-bg/85 p-4 backdrop-blur-sm"
          data-testid="puzzle-over"
        >
          <div class="card w-full max-w-sm p-6 text-center">
            <h2 class="mb-1 text-2xl font-extrabold">Игра окончена</h2>
            <p class="mb-1 text-lg">
              Счёт: <b>{{ finalScore ?? score }}</b>
            </p>
            <!-- Итог сервера отличается от набранного, если админ включил
                 множитель очков или лимит («подкрутка»). -->
            <p v-if="finalScore !== null && finalScore !== score" class="mb-1 text-xs text-muted">
              Набрано {{ score }} · зачтено {{ finalScore }}
            </p>
            <p class="mb-2 text-sm text-muted">Лучшее комбо: {{ bestCombo }}</p>
            <p v-if="rewardCreated" class="mb-1 text-sm font-semibold text-accent-deep">
              🎁 Награда получена!
            </p>
            <p v-if="bonusGranted" class="mb-1 text-sm font-semibold text-accent-deep">
              ⭐ Бонус за серию начислен!
            </p>
            <p class="mb-4 text-xs text-muted">{{ finishStatus }}</p>

            <div class="mb-4 text-left">
              <div class="mb-2 text-sm font-semibold text-muted">Лидерборд</div>
              <ol class="flex flex-col gap-1">
                <li
                  v-for="(row, i) in sortedLeaderboard"
                  :key="`${row.user_name}-${i}`"
                  class="flex justify-between rounded-lg px-3 py-1.5 text-sm"
                  :class="row.user_name === auth.username ? 'bg-accent/20 font-semibold' : 'bg-surface'"
                >
                  <span><span class="mr-2 text-muted">{{ i + 1 }}.</span>{{ row.user_name }}</span>
                  <span>{{ row.score }}</span>
                </li>
                <li v-if="sortedLeaderboard.length === 0" class="px-3 py-1.5 text-sm text-muted">
                  Пока пусто
                </li>
              </ol>
            </div>

            <button
              type="button"
              class="btn btn--primary w-full"
              :disabled="starting"
              data-testid="puzzle-again"
              @click="startNewSession"
            >
              {{ starting ? 'Запуск…' : 'Играть снова' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Легенда -->
      <div class="flex flex-wrap justify-center gap-4 text-xs text-muted">
        <span class="inline-flex items-center gap-1.5">
          <span class="h-2.5 w-2.5 rounded-full" style="background: #9a958c" />Фрагмент · 10
        </span>
        <span class="inline-flex items-center gap-1.5">
          <span class="h-2.5 w-2.5 rounded-full bg-accent" />Золото · 50
        </span>
        <span class="inline-flex items-center gap-1.5">
          <span class="h-2.5 w-2.5 rounded-full bg-ink" />Бустер · 100
        </span>
        <span class="inline-flex items-center gap-1.5">
          <span class="h-2.5 w-2.5 rounded-full" style="background: #e5484d" />Бомба · −жизнь
        </span>
      </div>
    </div>
  </div>
</template>
