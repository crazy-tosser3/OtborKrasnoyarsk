import type { ThemeId } from '~/shared/types/game'

/** Палитры тем для канваса (ограниченные, ретро). */
export interface ThemePalette {
  skyTop: string
  skyMid: string
  skyBottom: string
  ground: string
  balloonMain: string
  balloonDark: string
  balloonLight: string
  basket: string
  line: string
  lineHot: string
  cloud: string
}

export const THEME_PALETTES: Record<ThemeId, ThemePalette> = {
  // Красная тема — закатное тёплое небо
  red: {
    skyTop: '#3a1f5c',
    skyMid: '#a83a5b',
    skyBottom: '#ff8a4c',
    ground: '#5a2b3a',
    balloonMain: '#ff5a4d',
    balloonDark: '#b32d2d',
    balloonLight: '#ff9a7a',
    basket: '#7a4a22',
    line: '#ffd6a5',
    lineHot: '#ffe98a',
    cloud: '#ffd0b0',
  },
  // Зелёная тема — дневное голубое небо
  green: {
    skyTop: '#1e6bd6',
    skyMid: '#57b7ff',
    skyBottom: '#bfeaff',
    ground: '#2a8f45',
    balloonMain: '#4fd66f',
    balloonDark: '#2a8f45',
    balloonLight: '#a6f5b8',
    basket: '#7a4a22',
    line: '#ffffff',
    lineHot: '#ffe98a',
    cloud: '#ffffff',
  },
}
