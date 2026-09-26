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

// Lembretes por Web Push entram aqui numa fase seguinte (eventos 'push' e 'notificationclick').
