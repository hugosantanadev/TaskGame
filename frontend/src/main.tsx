import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router/dom'

import '@fontsource-variable/atkinson-hyperlegible-next'
import '@fontsource-variable/bricolage-grotesque'
import './styles/tokens.css'
import './styles/global.css'

import { queryClient } from './app/queryClient'
import { router } from './app/router'
import { UpdatePrompt } from './app/UpdatePrompt'
import { AuthProvider } from './auth/AuthProvider'

const root = document.getElementById('root')
if (!root) throw new Error('Elemento #root não encontrado no index.html')

createRoot(root).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
        <UpdatePrompt />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)
