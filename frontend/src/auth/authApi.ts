import { publicRequest } from '../api/client'
import type { AuthResponse } from '../api/types'

export type LoginInput = { email: string; password: string }
export type RegisterInput = { displayName: string; email: string; password: string; timeZone: string }

export const authApi = {
  login: (input: LoginInput) => publicRequest<AuthResponse>('/auth/login', { method: 'POST', body: input }),
  register: (input: RegisterInput) => publicRequest<AuthResponse>('/auth/register', { method: 'POST', body: input }),
  logout: () => publicRequest<void>('/auth/logout', { method: 'POST' }),
}
