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
  PAYLOAD_TOO_LARGE: 'A foto passou de 5 MB. Tire outra ou envie uma menor.',
  DAY_LOCKED: 'Hoje e os dias passados não aceitam essa mudança. Ela vale a partir de amanhã.',
  DAILY_LIMIT_REACHED: 'Esse dia já está cheio: o limite é de 10 obrigatórias e 5 extras.',
  WEEK_NOT_AVAILABLE: 'Só dá para planejar a semana atual e a próxima.',
  OCCURRENCE_ALREADY_ON_DATE: 'Essa missão já está planejada nesse dia.',
  OCCURRENCE_NOT_PENDING: 'Essa tarefa já foi concluída ou perdida.',
  NOT_COMPLETABLE_TODAY: 'Uma tarefa só pode ser concluída no próprio dia.',
  PROOF_REQUIRED: 'Essa missão pede uma foto como prova.',
  PROOF_ALREADY_ATTACHED: 'Essa tarefa já tem foto.',
  INVALID_IMAGE: 'Envie uma foto em JPEG, PNG ou WebP.',
  TASK_ARCHIVED: 'Essa missão está arquivada.',
  INVALID_SCHEDULE: 'Cada dia da semana pode aparecer só uma vez.',
  INSUFFICIENT_COINS: 'Faltam moedas para essa compra. Conclua mais tarefas e volte aqui.',
  ITEM_ALREADY_OWNED: 'Esse item já está na sua coleção.',
  ITEM_NOT_AVAILABLE: 'Esse item saiu da loja.',
  ITEM_NOT_FOR_ROOM: 'Esse item é do personagem, não do quarto.',
  ITEM_NOT_FOR_SLOT: 'Esse item não serve nesse lugar do personagem.',
}

/** Códigos em que o texto do servidor é mais específico que o genérico (diz o dia ou o limite). */
const DETAIL_FIRST = new Set(['DAY_LOCKED', 'DAILY_LIMIT_REACHED'])

export function messageFor(code: string, detail?: string): string {
  if (detail && DETAIL_FIRST.has(code)) return detail
  return MESSAGES[code] ?? detail ?? 'Não foi possível concluir a ação. Tente de novo.'
}

/** Qualquer falha vira ApiError, para as telas tratarem um formato só. */
export function asApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  return new ApiError(0, 'NETWORK_ERROR', messageFor('NETWORK_ERROR'))
}
