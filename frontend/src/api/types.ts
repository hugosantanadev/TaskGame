/** Formatos das respostas da API (espelham os DTOs do backend). */

export type User = {
  id: string
  email: string
  displayName: string
  timeZone: string
  rankingVisible: boolean
  createdAt: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
  user: User
}

export type FieldViolation = {
  field: string
  message: string
}

/** Problem Details (RFC 9457) com o `code` estável do GasmTask. */
export type ProblemDetails = {
  type?: string
  title?: string
  status?: number
  detail?: string
  code?: string
  errors?: FieldViolation[]
}
