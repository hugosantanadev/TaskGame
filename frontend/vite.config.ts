import { fileURLToPath } from 'node:url'

import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

const PAPER = '#f7f9fa'

export default defineConfig(({ mode }) => {
  // O mesmo .env da raiz que o backend lê: SERVER_PORT define a porta da API em desenvolvimento.
  const rootEnv = loadEnv(mode, fileURLToPath(new URL('..', import.meta.url)), '')
  const apiPort = rootEnv.SERVER_PORT || '8080'

  return {
    plugins: [
      react(),
      VitePWA({
        // Service worker próprio (src/sw.ts): hoje só cache do app; depois, Web Push.
        strategies: 'injectManifest',
        srcDir: 'src',
        filename: 'sw.ts',
        registerType: 'prompt',
        injectRegister: false,
        injectManifest: {
          globPatterns: ['**/*.{js,css,html,svg,png,woff2}'],
        },
        manifest: {
          name: 'GasmTask',
          short_name: 'GasmTask',
          description: 'Tarefas da vida real viram pontos, moedas e dias seguidos.',
          lang: 'pt-BR',
          start_url: '/',
          scope: '/',
          display: 'standalone',
          orientation: 'portrait',
          background_color: PAPER,
          theme_color: PAPER,
          icons: [
            { src: '/icons/pwa-192.png', sizes: '192x192', type: 'image/png' },
            { src: '/icons/pwa-512.png', sizes: '512x512', type: 'image/png' },
            { src: '/icons/pwa-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
          ],
        },
        devOptions: { enabled: false },
      }),
    ],
    server: {
      // Em desenvolvimento a API roda no Spring Boot; o proxy mantém tudo na mesma origem (sem CORS).
      proxy: { '/api': `http://localhost:${apiPort}` },
      // Permite abrir o servidor de desenvolvimento por um túnel HTTPS para testar no celular.
      allowedHosts: ['.trycloudflare.com'],
    },
  }
})
