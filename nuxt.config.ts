import tailwindcss from '@tailwindcss/vite'

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  // Игра работает целиком на клиенте: Canvas, таймеры, localStorage, WebAudio (позже).
  ssr: false,

  modules: ['@pinia/nuxt', '@nuxtjs/color-mode'],

  // Тёмная/светлая тема: класс .dark / .light вешается на <html>.
  colorMode: {
    preference: 'light',
    fallback: 'light',
    classSuffix: '',
  },

  vite: {
    plugins: [tailwindcss()],
  },

  runtimeConfig: {
    public: {
      // Фиксированный seed для воспроизводимых демо/проверок. Пусто = случайный seed.
      devFixedSeed: process.env.NUXT_PUBLIC_DEV_FIXED_SEED || '',
      // Бэкенд User API (свагер: аккаунты/лидерборд/награды/турнир) — отдельный сервис,
      // движок раунда шара по-прежнему на MockGameApi.
      userApiBase: process.env.NUXT_PUBLIC_USER_API_BASE || 'http://localhost:8000',
    },
  },

  app: {
    head: {
      title: 'Воздушный Шар',
      meta: [
        { charset: 'utf-8' },
        {
          name: 'viewport',
          content: 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no',
        },
      ],
      // Inter — чистый гротеск для минималистичной ч/б типографики.
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap',
        },
      ],
    },
  },

  css: ['~/assets/styles/main.css'],
})
