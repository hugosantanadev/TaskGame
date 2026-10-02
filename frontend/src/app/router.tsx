import { createBrowserRouter } from 'react-router'

import { AppShell } from '../components/AppShell'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { AchievementsPage } from '../features/achievements/AchievementsPage'
import { ExtraFormPage } from '../features/missions/ExtraFormPage'
import { MissionFormPage } from '../features/missions/MissionFormPage'
import { MissionsPage } from '../features/missions/MissionsPage'
import { WeekPage } from '../features/planning/WeekPage'
import { RankingPage } from '../features/ranking/RankingPage'
import { CharacterPage } from '../features/profile/CharacterPage'
import { RoomPage } from '../features/profile/RoomPage'
import { StatsPage } from '../features/stats/StatsPage'
import { StorePage } from '../features/store/StorePage'
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
              { path: '/semana', element: <WeekPage /> },
              { path: '/missoes', element: <MissionsPage /> },
              { path: '/missoes/nova', element: <MissionFormPage /> },
              { path: '/missoes/:id', element: <MissionFormPage /> },
              { path: '/extras/nova', element: <ExtraFormPage /> },
              { path: '/estatisticas', element: <StatsPage /> },
              { path: '/loja', element: <StorePage /> },
              { path: '/ranking', element: <RankingPage /> },
              { path: '/perfil', element: <ProfilePage /> },
              { path: '/perfil/conquistas', element: <AchievementsPage /> },
              { path: '/perfil/quarto', element: <RoomPage /> },
              { path: '/perfil/personagem', element: <CharacterPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
