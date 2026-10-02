import type { CompletionResult, ProofAttached } from '../api/types'

/**
 * Canal de feedback de recompensa: quem conclui publica, quem mostra (o aviso na tela, e no futuro
 * a camada visual do quarto e do personagem) escuta. Nenhuma tela precisa conhecer a outra.
 */
export type RewardEvent =
  | { kind: 'completed'; title: string; result: CompletionResult }
  | { kind: 'proof'; title: string; result: ProofAttached }
  | { kind: 'purchase'; title: string; balance: number }

type Listener = (event: RewardEvent) => void

const listeners = new Set<Listener>()

export function publishReward(event: RewardEvent): void {
  listeners.forEach((listener) => listener(event))
}

export function onReward(listener: Listener): () => void {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}
