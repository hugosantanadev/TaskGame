/// <reference lib="webworker" />
import { cleanupOutdatedCaches, createHandlerBoundToURL, precacheAndRoute } from 'workbox-precaching'
import { NavigationRoute, registerRoute } from 'workbox-routing'

declare let self: ServiceWorkerGlobalScope

// Só os arquivos do app (JS, CSS, fontes, ícones) ficam em cache. A API sempre vai à rede:
// dado autenticado em cache poderia mostrar informação velha ou de outra conta.
cleanupOutdatedCaches()
precacheAndRoute(self.__WB_MANIFEST)

// Navegação do SPA: qualquer rota do app abre o index.html do cache, inclusive offline.
registerRoute(
  new NavigationRoute(createHandlerBoundToURL('index.html'), {
    denylist: [/^\/api\//, /^\/swagger-ui/, /^\/v3\/api-docs/, /^\/actuator/],
  }),
)

// O aviso "Nova versão disponível" pede para a versão nova assumir.
self.addEventListener('message', (event) => {
  if (event.data?.type === 'SKIP_WAITING') {
    void self.skipWaiting()
  }
})

// Tocar num lembrete (mostrado pelo app em segundo plano) traz o app para a frente, ou abre a tela Hoje.
self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
      const open = windows.find((client) => 'focus' in client)
      return open ? open.focus() : self.clients.openWindow('/')
    }),
  )
})

// Lembretes por Web Push, com o app fechado, entram aqui numa fase seguinte (evento 'push').
