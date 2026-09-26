import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'

import { endSessionLocally, onSessionChange, refreshSession, startSession } from '../api/client'
import type { User } from '../api/types'
import { authApi, type LoginInput, type RegisterInput } from './authApi'
import { AuthContext, type AuthContextValue, type AuthState } from './context'

/**
 * Dono do estado de sessão. Ao abrir o app, tenta renovar a sessão pelo cookie (login silencioso);
 * o resultado chega pelo onSessionChange, assim como qualquer renovação feita pelo cliente HTTP.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [state, setState] = useState<AuthState>({ status: 'checking' })

  useEffect(
    () =>
      onSessionChange((session) => {
        if (session) {
          setState({ status: 'authenticated', user: session.user })
        } else {
          setState({ status: 'anonymous' })
          queryClient.clear() // nada do usuário anterior fica em cache
        }
      }),
    [queryClient],
  )

  // Login silencioso ao abrir o app. Sessão válida ou 401 chegam pelo onSessionChange;
  // aqui só sobra a falha de rede.
  useEffect(() => {
    refreshSession().catch(() => setState({ status: 'offline' }))
  }, [])

  const retry = useCallback(() => {
    setState({ status: 'checking' })
    refreshSession().catch(() => setState({ status: 'offline' }))
  }, [])

  const login = useCallback(async (input: LoginInput) => {
    startSession(await authApi.login(input))
  }, [])

  const register = useCallback(async (input: RegisterInput) => {
    startSession(await authApi.register(input))
  }, [])

  const logout = useCallback(async () => {
    // Sem rede, a sessão local termina mesmo assim.
    await authApi.logout().catch(() => undefined)
    endSessionLocally()
  }, [])

  const updateUser = useCallback((user: User) => {
    setState({ status: 'authenticated', user })
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ state, login, register, logout, retry, updateUser }),
    [state, login, register, logout, retry, updateUser],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
