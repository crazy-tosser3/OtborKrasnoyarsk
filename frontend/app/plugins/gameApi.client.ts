import { MockGameApi } from '~/shared/api/mock/MockGameApi'
import type { GameApi } from '~/shared/api/GameApi'

/**
 * Внедряет реализацию GameApi в приложение. Сейчас — MockGameApi («сервер в
 * браузере»). Позже здесь будет выбор HttpGameApi по NUXT_PUBLIC_API_MODE, без
 * правок компонентов.
 */
export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  let fixedSeed = (config.public.devFixedSeed as string) || undefined

  // Переопределение seed через ?seed=... — для воспроизводимых e2e/демо.
  if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search)
    const urlSeed = params.get('seed')
    const urlBalance = params.get('balance')
    if (urlSeed) {
      fixedSeed = urlSeed
      // Чистый старт: сбрасываем сохранённое состояние, чтобы счётчик раундов начинался с 0
      try {
        Object.keys(localStorage)
          .filter((k) => k.startsWith('balloon:'))
          .forEach((k) => localStorage.removeItem(k))
      } catch {
        /* ignore */
      }
    }
    // Тестовый хук: стартовый баланс через ?balance=
    if (urlBalance != null && Number.isFinite(Number(urlBalance))) {
      try {
        localStorage.setItem('balloon:balance', String(Number(urlBalance)))
      } catch {
        /* ignore */
      }
    }
  }

  const api: GameApi = new MockGameApi({ fixedSeed })

  return {
    provide: {
      gameApi: api,
    },
  }
})
