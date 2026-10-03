import { describe, expect, it } from 'vitest'

import { compareRanks, rankLabel, rankProgress, sameRank } from './rank'

describe('elos', () => {
  it('nome com divisão, menos na Lenda', () => {
    expect(rankLabel({ tier: 'SILVER', division: 2 })).toBe('Prata 2')
    expect(rankLabel({ tier: 'LEGEND', division: null })).toBe('Lenda')
  })

  it('compara degraus pela escada', () => {
    expect(compareRanks({ tier: 'BRONZE', division: 1 }, { tier: 'IRON', division: 3 })).toBeGreaterThan(0)
    expect(compareRanks({ tier: 'GOLD', division: 1 }, { tier: 'GOLD', division: 2 })).toBeLessThan(0)
    expect(compareRanks({ tier: 'LEGEND', division: null }, { tier: 'MASTER', division: 3 })).toBeGreaterThan(0)
    expect(sameRank({ tier: 'GOLD', division: 2 }, { tier: 'GOLD', division: 2 })).toBe(true)
  })

  it('a barra vai do início do degrau ao próximo e enche no topo', () => {
    expect(rankProgress({ tier: 'IRON', division: 2, xp: 75, rankStartXp: 50, nextRankXp: 100 })).toBe(0.5)
    expect(rankProgress({ tier: 'LEGEND', division: null, xp: 9000, rankStartXp: 7000, nextRankXp: null })).toBe(1)
  })
})
