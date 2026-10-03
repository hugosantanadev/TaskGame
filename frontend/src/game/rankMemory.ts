import type { RankRef } from '../api/types'

/*
 * Último elo que a pessoa viu, por conta, neste aparelho. Serve para avisar quando o elo muda fora de uma
 * conclusão (cair na virada do dia, por exemplo). É só conveniência: sem armazenamento, nada quebra.
 */

function key(userId: string): string {
  return `gasmtask-rank-${userId}`
}

export function readSeenRank(userId: string): RankRef | null {
  try {
    const raw = localStorage.getItem(key(userId))
    return raw ? (JSON.parse(raw) as RankRef) : null
  } catch {
    return null
  }
}

export function writeSeenRank(userId: string, rank: RankRef): void {
  try {
    localStorage.setItem(key(userId), JSON.stringify({ tier: rank.tier, division: rank.division }))
  } catch {
    // modo privado ou armazenamento bloqueado: o aviso de mudança de elo só não aparece
  }
}
