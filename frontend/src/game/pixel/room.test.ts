import { describe, expect, it } from 'vitest'

import { roomLayout } from './room'

describe('quarto', () => {
  it('desenha do fundo para a frente, ignorando itens sem lugar', () => {
    const codes = roomLayout(['rug_round', 'poster_space', 'bed_cozy', 'item_novo']).map((placed) => placed.code)
    expect(codes).toEqual(['poster_space', 'bed_cozy', 'rug_round'])
  })

  it('põe o que ia em cima da escrivaninha no chão quando ela não está no quarto', () => {
    const withDesk = roomLayout(['desk_simple', 'lamp_desk']).find((placed) => placed.code === 'lamp_desk')
    const withoutDesk = roomLayout(['lamp_desk']).find((placed) => placed.code === 'lamp_desk')
    expect(withDesk?.spot.y).toBeLessThan(withoutDesk?.spot.y ?? 0)
  })
})
