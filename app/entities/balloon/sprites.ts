import type { ThemePalette } from './palette'

/**
 * Пиксельные спрайты рисуются кодом прямоугольниками в целочисленных координатах.
 * Никаких внешних ассетов. Всё выравнивается по целым пикселям виртуального холста.
 */

function px(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, c: string) {
  ctx.fillStyle = c
  ctx.fillRect(Math.round(x), Math.round(y), Math.round(w), Math.round(h))
}

/**
 * Воздушный шар с корзиной. Центр — (cx, cy) на уровне корзины.
 *
 * Купол рисуется процедурно (не по ASCII-маске): цвет каждого блока считается по
 * расстоянию/углу до центра сферы, за счёт чего получается плавный объёмный
 * градиент, вертикальные швы-панели («гoры») классического шара и блик —
 * значительно детальнее старой 16×16 маски в 3 плоских тона.
 */
export function drawBalloon(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  pal: ThemePalette,
  sway = 0,
) {
  const block = 2 // размер условного пикселя
  const ox = Math.round(cx + sway)
  const oy = Math.round(cy)

  // --- геометрия: корзина внизу, шея-переход, купол сверху ---
  const bw = 7 * block
  const bh = 6 * block
  const basketTop = oy - bh
  const neckY = basketTop - 5 * block
  const R = 11 * block // радиус купола
  const domeCenterY = neckY - R * 0.72 // купол слегка «сидит» на шее, не идеальный круг

  // Направление света (сверху слева) — определяет блик и затенение.
  const lightAngle = -2.35
  const lx = Math.cos(lightAngle)
  const ly = Math.sin(lightAngle)
  const gores = 8 // число вертикальных панелей купола

  for (let dy = -R; dy <= R; dy += block) {
    for (let dx = -R; dx <= R; dx += block) {
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist > R) continue
      // слегка сплющиваем низ купола, чтобы он плавно сужался к шее
      if (dy > R * 0.55 && Math.abs(dx) > (R - dy) * 1.3) continue

      const nx = dx / R
      const ny = dy / R
      let shade = nx * lx + ny * ly // -1..1
      shade = (shade + 1) / 2 // 0..1

      const angle = Math.atan2(dy, dx)
      const goreT = (((angle + Math.PI) / (Math.PI * 2)) * gores) % 1
      const seam = (goreT < 0.05 || goreT > 0.95) && dist > R * 0.3

      let color: string
      if (seam) color = pal.balloonDark
      else if (shade > 0.74) color = pal.balloonLight
      else if (shade > 0.3) color = pal.balloonMain
      else color = pal.balloonDark

      px(ctx, ox + dx, domeCenterY + dy, block, block, color)
    }
  }

  // Точечный блик (глянец) в стороне света.
  const hlx = ox + lx * R * 0.45
  const hly = domeCenterY + ly * R * 0.45
  px(ctx, hlx - block, hly - block, block * 2, block * 2, '#ffffff')

  // Шея — переход купола в стропы.
  px(ctx, ox - 2 * block, neckY - 2 * block, 4 * block, 4 * block, pal.balloonDark)

  // Стропы (4 шт., веерами к углам корзины) — светлее одной сплошной линии.
  const ropeTopY = neckY + 2 * block
  const ropeBottomY = basketTop
  const ropeOffsets = [-3 * block, -1 * block, block, 3 * block]
  for (const offX of ropeOffsets) {
    const bottomX = ox + offX * 1.15
    drawLine(ctx, ox + offX * 0.3, ropeTopY, bottomX, ropeBottomY, pal.balloonDark)
  }

  // Корзина с плетёной текстурой вместо плоской заливки.
  px(ctx, ox - bw / 2, basketTop, bw, bh, pal.basket)
  for (let row = 0; row < bh; row += block) {
    const shiftIn = row % (block * 2) === 0
    for (let col = shiftIn ? 0 : block; col < bw; col += block * 2) {
      px(ctx, ox - bw / 2 + col, basketTop + row, block, block, '#00000022')
    }
  }
  px(ctx, ox - bw / 2, basketTop, bw, block, '#c98a4a') // светлый верхний край
  px(ctx, ox - bw / 2, basketTop + bh - block, bw, block, '#5a3616') // тёмный нижний край
  px(ctx, ox - bw / 2 - block, basketTop, block, bh, pal.balloonDark) // левая стойка
  px(ctx, ox + bw / 2, basketTop, block, bh, pal.balloonDark) // правая стойка
}

/** Пиксельная линия толщиной ~1 «блок» (для строп/канатов). */
function drawLine(
  ctx: CanvasRenderingContext2D,
  x0: number,
  y0: number,
  x1: number,
  y1: number,
  color: string,
) {
  const steps = Math.max(1, Math.round(Math.abs(y1 - y0) / 2))
  ctx.fillStyle = color
  for (let i = 0; i <= steps; i++) {
    const t = i / steps
    const x = x0 + (x1 - x0) * t
    const y = y0 + (y1 - y0) * t
    ctx.fillRect(Math.round(x), Math.round(y), 2, 2)
  }
}

/** Иконка бустера — сундук/подарок с ×N. */
export function drawBooster(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  tier: number,
  active: boolean,
) {
  const s = 2
  const box = active ? '#ffd23f' : '#c9a24b'
  const dark = active ? '#b8860b' : '#7a5a1a'
  px(ctx, x - 6 * s, y - 6 * s, 12 * s, 10 * s, box)
  px(ctx, x - 6 * s, y - 6 * s, 12 * s, 2 * s, dark)
  // лента
  px(ctx, x - s, y - 6 * s, 2 * s, 10 * s, dark)
  // подпись ×N
  ctx.fillStyle = '#2a1a05'
  ctx.font = '700 9px ui-sans-serif, system-ui, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(`×${tier}`, x, y + s)
}

/** Простое пиксельное облако. */
export function drawCloud(ctx: CanvasRenderingContext2D, x: number, y: number, c: string, scale = 2) {
  const s = scale
  const rows = ['00111100', '01111110', '11111111', '01111100']
  for (let r = 0; r < rows.length; r++) {
    for (let col = 0; col < rows[r]!.length; col++) {
      if (rows[r]![col] === '1') px(ctx, x + col * s, y + r * s, s, s, c)
    }
  }
}
