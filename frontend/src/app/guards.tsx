import { Navigate, Outlet, useLocation } from 'react-router'

import { useAuth } from '../auth/context'
import { SessionScreen } from './SessionScreen'

/** Rotas do app: sem sessão, vai para /entrar lembrando de onde veio. */
export function RequireAuth() {
  const { state } = useAuth()
  const location = useLocation()

  if (state.status === 'authenticated') return <Outlet />
  if (state.status === 'anonymous') {
    return <Navigate to="/entrar" replace state={{ from: location.pathname + location.search }} />
  }
  return <SessionScreen />
}

/** Entrar e cadastro: com sessão ativa, volta para onde a pessoa queria ir. */
export function GuestOnly() {
  const { state } = useAuth()
  const location = useLocation()

  if (state.status === 'anonymous') return <Outlet />
  if (state.status === 'authenticated') {
    const from = (location.state as { from?: string } | null)?.from
    return <Navigate to={from && !from.startsWith('/entrar') ? from : '/'} replace />
  }
  return <SessionScreen />
}
