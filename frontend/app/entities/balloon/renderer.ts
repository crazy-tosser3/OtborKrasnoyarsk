import type { ThemePalette } from './palette'
import { drawBalloon, drawBooster, drawCloud } from './sprites'

/** Виртуальное разрешение сцены (портрет). Масштабируется целочисленно. */
export const VW = 340
export const VH = 340

const BASE_SCREEN_Y = Math.round(VH * 0.68) // шар зафиксирован здесь, камера скроллит
const LEVEL_GAP = 46 // равномерный шаг между уровнями в мировых пикселях

/**
 * Мировая координата (px вверх от m=1). Кусочно-линейная по индексу уровня:
 * уровень i находится на (i+1)*LEVEL_GAP, поэтому шаг между линиями одинаковый,
 * а камера скроллит за шаром — уровни появляются постепенно по мере подъёма.
 */
function worldY(m: number, levels: number[]): number {
  if (levels.length === 0) return Math.max(0, m - 1) * LEVEL_GAP
  let prevM = 1
  let prevY = 0
  for (let i = 0; i < levels.length; i++) {
    const curM = levels[i]!
    const curY = (i + 1) * LEVEL_GAP
    if (m <= curM) {
      const span = curM - prevM || 1
      return prevY + ((m - prevM) / span) * (curY - prevY)
    }
    prevM = curM
    prevY = curY
  }
  const li = levels.length - 1
  const prevPrevM = li > 0 ? levels[li - 1]! : 1
  const slope = LEVEL_GAP / ((levels[li]! - prevPrevM) || 1)
  return prevY + (m - levels[li]!) * slope
}

/** Шар всегда на BASE_SCREEN_Y (камера следует за ним). */
export function balloonScreenY(_m?: number, _levels?: number[]): number {
  return BASE_SCREEN_Y
}

export interface Cloud {
  worldY: number
  x: number
  scale: number
}

export interface SceneState {
  pal: ThemePalette
  baseMultiplier: number
  levels: number[]
  boosterLevel: number | null
  boosterTier: number
  boosterActive: boolean
  swayT: number
  clouds: Cloud[]
  crashed: boolean
}

/** Генерация набора облаков для фона (worldY в тех же пикселях, что и уровни). */
export function makeClouds(rand: () => number): Cloud[] {
  const clouds: Cloud[] = []
  for (let i = 0; i < 8; i++) {
    clouds.push({
      worldY: i * 90 + rand() * 60,
      x: 10 + rand() * (VW - 40),
      scale: rand() < 0.5 ? 2 : 3,
    })
  }
  return clouds
}

function skyGradient(ctx: CanvasRenderingContext2D, pal: ThemePalette) {
  const g = ctx.createLinearGradient(0, 0, 0, VH)
  g.addColorStop(0, pal.skyTop)
  g.addColorStop(0.55, pal.skyMid)
  g.addColorStop(1, pal.skyBottom)
  ctx.fillStyle = g
  ctx.fillRect(0, 0, VW, VH)
  // лёгкий дизеринг — ряды точек на стыке
  ctx.fillStyle = pal.skyMid
  for (let x = 0; x < VW; x += 4) {
    ctx.fillRect(x, Math.round(VH * 0.55), 2, 2)
  }
}

export function drawScene(ctx: CanvasRenderingContext2D, st: SceneState) {
  ctx.imageSmoothingEnabled = false
  skyGradient(ctx, st.pal)

  const n = st.levels.length
  const curY = worldY(st.baseMultiplier, st.levels)

  // Облака — параллакс относительно подъёма.
  for (const c of st.clouds) {
    const screenY = BASE_SCREEN_Y - (c.worldY - curY) * 0.6
    if (screenY > -20 && screenY < VH + 20) {
      drawCloud(ctx, c.x, screenY, st.pal.cloud, c.scale)
    }
  }

  // Уровни — пунктирные линии + плашка с коэффициентом; появляются постепенно
  // по мере подъёма (камера скроллит за шаром).
  ctx.font = '700 9px ui-sans-serif, system-ui, sans-serif'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  const LABEL_W = 26
  for (let i = 0; i < n; i++) {
    const t = st.levels[i]!
    const screenY = Math.round(BASE_SCREEN_Y - (worldY(t, st.levels) - curY))
    if (screenY < -10 || screenY > VH + 10) continue
    const passed = st.baseMultiplier >= t
    const isBooster = st.boosterLevel === i
    const col = isBooster ? st.pal.lineHot : passed ? st.pal.line : 'rgba(255,255,255,0.45)'

    // пунктир от плашки до правого края
    for (let x = LABEL_W + 8; x < VW - 8; x += 10) {
      ctx.fillStyle = col
      ctx.fillRect(x, screenY, 5, 1)
    }
    // плашка уровня (число рисуется отдельно, чётко — см. drawLevelLabels)
    ctx.fillStyle = passed || isBooster ? st.pal.lineHot : 'rgba(0,0,0,0.35)'
    ctx.fillRect(2, screenY - 6, LABEL_W, 12)

    // маркер бустера справа
    if (isBooster) {
      drawBooster(ctx, VW - 20, screenY - 12, st.boosterTier, st.boosterActive)
    }
  }

  // Шар зафиксирован по центру, камера следует за ним.
  if (!st.crashed) {
    const sway = Math.sin(st.swayT * 2) * 3
    drawBalloon(ctx, VW / 2, BASE_SCREEN_Y, st.pal, sway)
  }
}

/**
 * Номера уровней рисуются ОТДЕЛЬНО на видимом холсте (в его нативном разрешении),
 * а не на пиксельном оффскрине — иначе текст размывается при масштабировании.
 * scale = display.width / VW; dx/dy — смещение тряски, совпадающее с блитом сцены.
 */
export function drawLevelLabels(
  ctx: CanvasRenderingContext2D,
  st: SceneState,
  scale: number,
  dx = 0,
  dy = 0,
) {
  const curY = worldY(st.baseMultiplier, st.levels)
  ctx.save()
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  ctx.font = `700 ${Math.round(10 * scale)}px ui-sans-serif, system-ui, sans-serif`
  for (let i = 0; i < st.levels.length; i++) {
    const t = st.levels[i]!
    const screenY = BASE_SCREEN_Y - (worldY(t, st.levels) - curY)
    if (screenY < -10 || screenY > VH + 10) continue
    const passed = st.baseMultiplier >= t || st.boosterLevel === i
    ctx.fillStyle = passed ? '#2a1a05' : '#ffffff'
    ctx.fillText(`${(i + 1).toFixed(1)}`, 5 * scale + dx, screenY * scale + dy)
  }
  ctx.restore()
}

/** Система частиц взрыва шара. */
export class Explosion {
  private parts: {
    x: number
    y: number
    vx: number
    vy: number
    life: number
    max: number
    c: string
    s: number
  }[] = []

  spawn(x: number, y: number, pal: ThemePalette, rand: () => number) {
    const colors = [pal.balloonMain, pal.balloonDark, pal.balloonLight, '#ffffff']
    for (let i = 0; i < 40; i++) {
      const ang = rand() * Math.PI * 2
      const sp = 20 + rand() * 90
      this.parts.push({
        x,
        y,
        vx: Math.cos(ang) * sp,
        vy: Math.sin(ang) * sp - 20,
        life: 0,
        max: 0.5 + rand() * 0.6,
        c: colors[Math.floor(rand() * colors.length)]!,
        s: rand() < 0.5 ? 2 : 3,
      })
    }
  }

  get active() {
    return this.parts.length > 0
  }

  update(dt: number) {
    for (const p of this.parts) {
      p.life += dt
      p.x += p.vx * dt
      p.y += p.vy * dt
      p.vy += 120 * dt // гравитация
    }
    this.parts = this.parts.filter((p) => p.life < p.max)
  }

  draw(ctx: CanvasRenderingContext2D) {
    for (const p of this.parts) {
      const alpha = 1 - p.life / p.max
      ctx.globalAlpha = Math.max(0, alpha)
      ctx.fillStyle = p.c
      ctx.fillRect(Math.round(p.x), Math.round(p.y), p.s, p.s)
    }
    ctx.globalAlpha = 1
  }
}

export const BALLOON_X = VW / 2
