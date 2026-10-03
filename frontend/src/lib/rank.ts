import type { RankRef, RankStatus, RankTier } from '../api/types'

export const TIER_LABELS: Record<RankTier, string> = {
  IRON: 'Ferro',
  BRONZE: 'Bronze',
  SILVER: 'Prata',
  GOLD: 'Ouro',
  PLATINUM: 'Platina',
  DIAMOND: 'Diamante',
  MASTER: 'Mestre',
  LEGEND: 'Lenda',
}

/** "Prata 2"; "Lenda" não tem divisão. */
export function rankLabel(rank: RankRef): string {
  const tier = TIER_LABELS[rank.tier]
  return rank.division === null ? tier : `${tier} ${rank.division}`
}

/** Cor do elo (tokens.css); sempre usada ao lado do nome, nunca sozinha. */
export function tierColor(tier: RankTier): string {
  return `var(--tier-${tier.toLowerCase()})`
}

export function sameRank(a: RankRef, b: RankRef): boolean {
  return a.tier === b.tier && a.division === b.division
}

const TIER_ORDER: RankTier[] = ['IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND', 'MASTER', 'LEGEND']

/** Positivo se `a` está acima de `b`, negativo se abaixo, zero se for o mesmo degrau. */
export function compareRanks(a: RankRef, b: RankRef): number {
  const position = (rank: RankRef) => TIER_ORDER.indexOf(rank.tier) * 3 + (rank.division ?? 3)
  return position(a) - position(b)
}

/** Progresso dentro do degrau atual, de 0 a 1; em Lenda a barra fica cheia. */
export function rankProgress(status: RankStatus): number {
  if (status.nextRankXp === null) return 1
  const span = status.nextRankXp - status.rankStartXp
  return span <= 0 ? 1 : Math.min(1, Math.max(0, (status.xp - status.rankStartXp) / span))
}
