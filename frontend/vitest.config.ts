import { defineConfig } from 'vitest/config'

// Testes das regras puras do app (datas, fuso, escalas, textos). Ficam fora do vite.config.ts para não
// carregar o plugin do PWA; o fuso é fixo para que "hoje" não dependa da máquina que roda os testes.
export default defineConfig({
  test: {
    include: ['src/**/*.test.ts'],
    environment: 'node',
    env: { TZ: 'UTC' },
  },
})
