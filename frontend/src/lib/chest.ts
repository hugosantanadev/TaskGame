import type { ChestTier } from '../api/types'
import type { ChestLook } from '../game/pixel/art/scenery'

export const CHEST_LABEL: Record<ChestTier, string> = {
  NONE: 'Sem baú',
  WOOD: 'Baú de madeira',
  SILVER: 'Baú de prata',
  GOLD: 'Baú de ouro',
  LEGENDARY: 'Baú lendário',
}

/** Dias cumpridos que cada baú pede, na mesma regra do backend (ChestTier.forFulfilledDays). */
const STEPS: { tier: Exclude<ChestTier, 'NONE'>; days: number }[] = [
  { tier: 'WOOD', days: 2 },
  { tier: 'SILVER', days: 4 },
  { tier: 'GOLD', days: 6 },
  { tier: 'LEGENDARY', days: 7 },
]

export function chestTierFor(fulfilledDays: number): ChestTier {
  return [...STEPS].reverse().find((step) => fulfilledDays >= step.days)?.tier ?? 'NONE'
}

/** O próximo baú da semana e quantos dias faltam para ele; null quando já está no lendário. */
export function nextChest(fulfilledDays: number): { tier: ChestTier; missing: number } | null {
  const step = STEPS.find((candidate) => fulfilledDays < candidate.days)
  return step ? { tier: step.tier, missing: step.days - fulfilledDays } : null
}

/** Desenho do baú; um baú sem nível (não deveria aparecer) usa o de madeira. */
export function chestLook(tier: ChestTier): ChestLook {
  return tier === 'NONE' ? 'WOOD' : tier
}
