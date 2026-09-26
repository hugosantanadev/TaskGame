import { ApiError, messageFor } from './errors'
import type { AuthResponse, ProblemDetails } from './types'

/*
 * Cliente HTTP do app.
 * - O access token fica só em memória (nada de localStorage): some ao fechar a aba.
 * - O refresh token vive num cookie HttpOnly que o JavaScript não lê; o navegador o envia
 *   sozinho para /api/v1/auth/refresh.
 * - Uma resposta 401 dispara UMA renovação por vez (single-flight na aba + Web Locks entre abas)
 *   e a requisição é repetida uma única vez com o token novo.
 */

const BASE_URL = '/api/v1'
const REFRESH_LOCK = 'gasmtask-auth-refresh'

type SessionListener = (session: AuthResponse | null) => void

let accessToken: string | null = null
let refreshInFlight: Promise<AuthResponse | null> | null = null
const listeners = new Set<SessionListener>()

export type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  signal?: AbortSignal
}

/** Avisa quando a sessão começa, é renovada (com o usuário atualizado) ou termina (null). */
export function onSessionChange(listener: SessionListener): () => void {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

function publish(session: AuthResponse | null) {
  accessToken = session?.accessToken ?? null
  listeners.forEach((listener) => listener(session))
}

export function startSession(session: AuthResponse) {
  publish(session)
}

export function endSessionLocally() {
  publish(null)
}

/**
 * Troca o cookie de refresh por um novo access token. Resolve com null quando não há sessão
 * válida (401) e rejeita com ApiError quando o servidor não pôde ser consultado.
 */
export function refreshSession(): Promise<AuthResponse | null> {
  refreshInFlight ??= withRefreshLock(requestRefresh).finally(() => {
    refreshInFlight = null
  })
  return refreshInFlight
}

async function withRefreshLock<T>(task: () => Promise<T>): Promise<T> {
  if (!navigator.locks) return task()
  let result!: T
  await navigator.locks.request(REFRESH_LOCK, async () => {
    result = await task()
  })
  return result
}

async function requestRefresh(): Promise<AuthResponse | null> {
  const response = await send('/auth/refresh', { method: 'POST' }, null)
  if (response.ok) {
    const session = (await response.json()) as AuthResponse
    publish(session)
    return session
  }
  if (response.status === 401) {
    publish(null)
    return null
  }
  throw await toApiError(response)
}

/** Requisição autenticada. Renova a sessão e repete uma vez se o access token tiver vencido. */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  if (!accessToken && !(await refreshSession())) throw unauthenticated()

  const usedToken = accessToken
  let response = await send(path, options, usedToken)

  if (response.status === 401) {
    // Outra requisição pode já ter renovado enquanto esta estava no ar.
    const freshToken =
      accessToken && accessToken !== usedToken ? accessToken : ((await refreshSession())?.accessToken ?? null)
    if (!freshToken) throw unauthenticated()
    response = await send(path, options, freshToken)
  }
  return parse<T>(response)
}

/** Requisição sem access token (rotas /auth). */
export async function publicRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return parse<T>(await send(path, options, null))
}

async function send(path: string, options: RequestOptions, token: string | null): Promise<Response> {
  const headers = new Headers({ Accept: 'application/json, application/problem+json' })
  if (options.body !== undefined) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  try {
    return await fetch(`${BASE_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      credentials: 'same-origin',
      signal: options.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, 'NETWORK_ERROR', messageFor('NETWORK_ERROR'))
  }
}

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) throw await toApiError(response)
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

async function toApiError(response: Response): Promise<ApiError> {
  let problem: ProblemDetails | null = null
  try {
    problem = (await response.json()) as ProblemDetails
  } catch {
    // Corpo vazio ou HTML (ex.: 502 do proxy quando o backend está fora do ar)
  }
  const code = problem?.code ?? fallbackCode(response.status)
  const fieldErrors = Object.fromEntries((problem?.errors ?? []).map((error) => [error.field, error.message]))
  return new ApiError(response.status, code, messageFor(code, problem?.detail), fieldErrors)
}

function fallbackCode(status: number): string {
  if (status === 502 || status === 503 || status === 504) return 'SERVER_UNAVAILABLE'
  return status >= 500 ? 'INTERNAL_ERROR' : 'REQUEST_FAILED'
}

function unauthenticated(): ApiError {
  return new ApiError(401, 'UNAUTHENTICATED', messageFor('UNAUTHENTICATED'))
}
