import { createBrowserRouter } from 'react-router'

import { AppShell } from '../components/AppShell'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { MissionsPage } from '../features/missions/MissionsPage'
import { WeekPage } from '../features/planning/WeekPage'
import { TodayPage } from '../features/today/TodayPage'
import { NotFoundPage, RouteError } from './ErrorPages'
import { GuestOnly, RequireAuth } from './guards'
import { SessionScreen } from './SessionScreen'

/*
 * Hoje, Semana, Missões e as telas de entrada vêm no pacote principal: são as que abrem todo dia. O resto é
 * carregado na primeira visita, para o app abrir rápido no celular.
 */
export const router = createBrowserRouter([
  {
    errorElement: <RouteError />,
    hydrateFallbackElement: <SessionScreen />,
    children: [
      // Pública: aberta com ou sem sessão
      {
        path: '/bem-vindo',
        lazy: async () => ({ Component: (await import('../features/landing/LandingPage')).LandingPage }),
      },
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
              { path: '/semana', element: <WeekPage /> },
              { path: '/missoes', element: <MissionsPage /> },
              {
                path: '/missoes/nova',
                lazy: async () => ({ Component: (await import('../features/missions/MissionFormPage')).MissionFormPage }),
              },
              {
                path: '/missoes/:id',
                lazy: async () => ({ Component: (await import('../features/missions/MissionFormPage')).MissionFormPage }),
              },
              {
                path: '/extras/nova',
                lazy: async () => ({ Component: (await import('../features/missions/ExtraFormPage')).ExtraFormPage }),
              },
              {
                path: '/estatisticas',
                lazy: async () => ({ Component: (await import('../features/stats/StatsPage')).StatsPage }),
              },
              {
                path: '/loja',
                lazy: async () => ({ Component: (await import('../features/store/StorePage')).StorePage }),
              },
              {
                path: '/ranking',
                lazy: async () => ({ Component: (await import('../features/ranking/RankingPage')).RankingPage }),
              },
              {
                path: '/ranking/elo',
                lazy: async () => ({ Component: (await import('../features/ranking/RankPage')).RankPage }),
              },
              {
                path: '/perfil',
                lazy: async () => ({ Component: (await import('../features/profile/ProfilePage')).ProfilePage }),
              },
              {
                path: '/perfil/conquistas',
                lazy: async () => ({ Component: (await import('../features/achievements/AchievementsPage')).AchievementsPage }),
              },
              {
                path: '/perfil/quarto',
                lazy: async () => ({ Component: (await import('../features/profile/RoomPage')).RoomPage }),
              },
              {
                path: '/perfil/personagem',
                lazy: async () => ({ Component: (await import('../features/profile/CharacterPage')).CharacterPage }),
              },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
