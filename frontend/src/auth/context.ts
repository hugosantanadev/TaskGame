import { createContext, useContext } from 'react'

import type { User } from '../api/types'
import type { LoginInput, RegisterInput } from './authApi'

export type AuthState =
  | { status: 'checking' }
  | { status: 'offline' }
  | { status: 'anonymous' }
  | { status: 'authenticated'; user: User }

export type AuthContextValue = {
  state: AuthState
  login: (input: LoginInput) => Promise<void>
  register: (input: RegisterInput) => Promise<void>
  logout: () => Promise<void>
  retry: () => void
  updateUser: (user: User) => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return value
}

/** Usuário logado. Só pode ser usado dentro das rotas protegidas por <RequireAuth>. */
export function useCurrentUser(): User {
  const { state } = useAuth()
  if (state.status !== 'authenticated') throw new Error('useCurrentUser fora de uma rota autenticada')
  return state.user
}
