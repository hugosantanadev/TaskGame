/** Erro de API já traduzido: `code` estável, mensagem pronta para a tela e erros por campo. */
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: Readonly<Record<string, string>>

  constructor(status: number, code: string, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

const MESSAGES: Record<string, string> = {
  INVALID_CREDENTIALS: 'E-mail ou senha incorretos.',
  EMAIL_ALREADY_REGISTERED: 'Já existe uma conta com este e-mail. Entre com ela ou use outro e-mail.',
  VALIDATION_FAILED: 'Revise os campos destacados.',
  MALFORMED_REQUEST: 'O servidor não entendeu os dados enviados. Atualize a página e tente de novo.',
  UNAUTHENTICATED: 'Sua sessão terminou. Entre novamente.',
  INVALID_REFRESH_TOKEN: 'Sua sessão terminou. Entre novamente.',
  ACCESS_DENIED: 'Você não tem permissão para esta ação.',
  RESOURCE_NOT_FOUND: 'Não encontramos o que você procurava.',
  CONCURRENT_MODIFICATION: 'Os dados mudaram enquanto você editava. Confira e salve de novo.',
  NETWORK_ERROR: 'Sem conexão com o servidor. Confira sua internet e tente de novo.',
  SERVER_UNAVAILABLE: 'O servidor está fora do ar agora. Tente de novo em instantes.',
  INTERNAL_ERROR: 'Erro inesperado no servidor. Tente de novo em instantes.',
}

export function messageFor(code: string, detail?: string): string {
  return MESSAGES[code] ?? detail ?? 'Não foi possível concluir a ação. Tente de novo.'
}

/** Qualquer falha vira ApiError, para as telas tratarem um formato só. */
export function asApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  return new ApiError(0, 'NETWORK_ERROR', messageFor('NETWORK_ERROR'))
}
