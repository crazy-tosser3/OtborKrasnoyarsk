<script setup lang="ts">
import { useGameStore } from '~/stores/game'
import { THEME_PALETTES } from '~/entities/balloon/palette'
import {
  BALLOON_X,
  Explosion,
  VH,
  VW,
  balloonScreenY,
  drawScene,
  drawLevelLabels,
  makeClouds,
  type Cloud,
  type SceneState,
} from '~/entities/balloon/renderer'

const store = useGameStore()

const display = ref<HTMLCanvasElement | null>(null)
let offscreen: HTMLCanvasElement | null = null
let octx: CanvasRenderingContext2D | null = null
let raf = 0
let lastTs = 0
let clouds: Cloud[] = []
let explosion = new Explosion()
let explosionSpawned = false
let shakeUntil = 0
let currentRoundId = ''

const reducedMotion =
  typeof window !== 'undefined' &&
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

function resize() {
  const canvas = display.value
  if (!canvas) return
  const parent = canvas.parentElement
  if (!parent) return
  const cw = parent.clientWidth
  const ch = parent.clientHeight
  // Целочисленный масштаб виртуального разрешения
  const scale = Math.max(1, Math.floor(Math.min(cw / VW, ch / VH)))
  canvas.width = VW * scale
  canvas.height = VH * scale
  canvas.style.width = `${VW * scale}px`
  canvas.style.height = `${VH * scale}px`
  const ctx = canvas.getContext('2d')
  if (ctx) ctx.imageSmoothingEnabled = false
}

function ensureClouds() {
  if (store.round && store.round.id !== currentRoundId) {
    currentRoundId = store.round.id
    clouds = makeClouds(Math.random)
    explosion = new Explosion()
    explosionSpawned = false
  }
}

function frame(ts: number) {
  raf = requestAnimationFrame(frame)
  const canvas = display.value
  if (!canvas || !octx || !offscreen) return
  if (document.hidden) {
    lastTs = ts
    return
  }
  const dt = lastTs ? Math.min(0.05, (ts - lastTs) / 1000) : 0
  lastTs = ts

  ensureClouds()

  // Продвигаем состояние раунда (авторитетную проверку краха делает store через API)
  store.tick(ts)

  const pal = THEME_PALETTES[store.theme]
  const crashed = store.phase === 'crashed'

  const state: SceneState = {
    pal,
    baseMultiplier: store.baseMultiplier,
    levels: store.round?.levels ?? [],
    boosterLevel: store.round?.boosterLevel ?? null,
    boosterTier: store.round?.boosterTier ?? 1,
    boosterActive: store.boosterActive,
    swayT: ts / 1000,
    clouds,
    crashed,
  }

  drawScene(octx, state)

  // Взрыв при крахе
  if (crashed && !explosionSpawned) {
    explosion.spawn(BALLOON_X, balloonScreenY(store.baseMultiplier, store.round?.levels ?? []) - 20, pal, Math.random)
    explosionSpawned = true
    if (!reducedMotion) shakeUntil = ts + 320
  }
  if (explosion.active) {
    explosion.update(dt)
    explosion.draw(octx)
  }

  // Блит на видимый холст с целочисленным масштабом + тряска
  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.imageSmoothingEnabled = false
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    const scale = canvas.width / VW
    let dx = 0
    let dy = 0
    if (ts < shakeUntil) {
      dx = (Math.random() - 0.5) * 6 * scale
      dy = (Math.random() - 0.5) * 6 * scale
    }
    ctx.drawImage(offscreen, dx, dy, canvas.width, canvas.height)
    // Номера уровней — чётким текстом поверх (не пиксельным апскейлом).
    drawLevelLabels(ctx, state, scale, dx, dy)
  }
}

onMounted(() => {
  offscreen = document.createElement('canvas')
  offscreen.width = VW
  offscreen.height = VH
  octx = offscreen.getContext('2d')
  if (octx) octx.imageSmoothingEnabled = false
  resize()
  window.addEventListener('resize', resize)
  raf = requestAnimationFrame(frame)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', resize)
})
</script>

<template>
  <div class="flex h-full w-full items-center justify-center overflow-hidden">
    <canvas ref="display" class="block [image-rendering:pixelated]" />
  </div>
</template>
