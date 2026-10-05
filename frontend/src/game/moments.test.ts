import { describe, expect, it } from 'vitest'

import type { CompletionResult, RankStatus } from '../api/types'
import { momentsOf } from './moments'

const gold: RankStatus = { tier: 'GOLD', division: 1, xp: 1500, rankStartXp: 1500, nextRankXp: 1650 }

function completion(overrides: { titles?: CompletionResult['attribute']['unlockedTitles']; promoted?: boolean }) {
  return {
    attribute: { unlockedTitles: overrides.titles ?? [] },
    xp: { gained: 10, status: gold, promoted: overrides.promoted ?? false, demoted: false, unlockedItems: [] },
  } as unknown as CompletionResult
}

describe('momentos de comemoração', () => {
  it('título novo e subida de elo viram comemoração, um de cada vez', () => {
    const moments = momentsOf({ kind: 'completed', title: 'Academia', result: completion({ titles: ['GYM_RAT'], promoted: true }) })
    expect(moments.map((moment) => moment.kind)).toEqual(['title', 'rank'])
  })

  it('conclusão comum e queda de elo ficam só no aviso pequeno', () => {
    expect(momentsOf({ kind: 'completed', title: 'Ler', result: completion({}) })).toEqual([])
    expect(momentsOf({ kind: 'rank', from: gold, to: { tier: 'SILVER', division: 3 } })).toEqual([])
    expect(momentsOf({ kind: 'rank', from: { tier: 'SILVER', division: 3 }, to: gold })).toHaveLength(1)
  })
})
