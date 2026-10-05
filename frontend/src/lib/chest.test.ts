import { describe, expect, it } from 'vitest'

import { chestTierFor, nextChest } from './chest'

describe('baú semanal', () => {
  it('segue a regra do backend: 2, 4, 6 e 7 dias', () => {
    expect([0, 1, 2, 3, 4, 5, 6, 7].map(chestTierFor)).toEqual([
      'NONE',
      'NONE',
      'WOOD',
      'WOOD',
      'SILVER',
      'SILVER',
      'GOLD',
      'LEGENDARY',
    ])
  })

  it('diz quanto falta para o próximo baú', () => {
    expect(nextChest(0)).toEqual({ tier: 'WOOD', missing: 2 })
    expect(nextChest(5)).toEqual({ tier: 'GOLD', missing: 1 })
    expect(nextChest(7)).toBeNull()
  })
})
