import { createBrowserRouter } from 'react-router'

import { AppShell } from '../components/AppShell'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { PlaceholderPage } from '../features/placeholder/PlaceholderPage'
import { ProfilePage } from '../features/profile/ProfilePage'
import { TodayPage } from '../features/today/TodayPage'
import { NotFoundPage, RouteError } from './ErrorPages'
import { GuestOnly, RequireAuth } from './guards'

export const router = createBrowserRouter([
  {
    errorElement: <RouteError />,
    children: [
      {
        element: <GuestOnly />,
        children: [
          { path: '/entrar', element: <LoginPage /> },
          { path: '/cadastro', element: <RegisterPage /> },
        ],
      },
      {
        element: <RequireAuth />,
        children: [
          {
            element: <AppShell />,
            children: [
              { path: '/', element: <TodayPage /> },
              {
                path: '/estatisticas',
                element: (
                  <PlaceholderPage
                    title="Estatísticas"
                    description="Aqui vão aparecer o planejado e o concluído por semana e por mês, pontos, moedas e sua maior sequência."
                  />
                ),
              },
              {
                path: '/loja',
                element: (
                  <PlaceholderPage
                    title="Loja"
                    description="Aqui você vai trocar moedas por móveis, decoração e roupas para o seu quarto e personagem."
                  />
                ),
              },
              {
                path: '/ranking',
                element: (
                  <PlaceholderPage
                    title="Ranking"
                    description="Aqui vai aparecer o ranking da semana por pontos, missões concluídas, moedas ganhas e sequência."
                  />
                ),
              },
              { path: '/perfil', element: <ProfilePage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
