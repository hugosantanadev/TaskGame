import { describe, expect, it } from 'vitest'

import { STARTER_ROOM } from '../../lib/equipment'
import { dressCharacter } from './art/character'
import { roomLayout } from './room'
import { spriteSize } from './sprite'

const keys = (placements: ReturnType<typeof roomLayout>) => placements.map((placement) => placement.key)

describe('quarto', () => {
  it('no primeiro dia tem só o colchão, a pilha de livros e o celular com teclado e mouse', () => {
    expect(keys(roomLayout({ equipment: STARTER_ROOM, items: [] })).sort()).toEqual([
      'equipment-BED',
      'equipment-BOOKSHELF',
      'equipment-COMPUTER',
    ])
  })

  it('põe o computador e a luminária em cima da mesa, e a caneca que não cabe vai para o chão', () => {
    const equipment = { ...STARTER_ROOM, DESK: 2, COMPUTER: 1 }
    const placements = roomLayout({ equipment, items: ['lamp_desk', 'mug_coffee'] })
    const desk = placements.find((placement) => placement.key === 'equipment-DESK')!
    const computer = placements.find((placement) => placement.key === 'equipment-COMPUTER')!
    const lamp = placements.find((placement) => placement.key === 'decor-lamp_desk')!
    const mug = placements.find((placement) => placement.key === 'decor-mug_coffee')!

    expect(computer.y + spriteSize(computer.sprite).height).toBe(desk.y)
    expect(lamp.y + spriteSize(lamp.sprite).height).toBe(desk.y)
    expect(mug.y + spriteSize(mug.sprite).height).toBeGreaterThan(desk.y)
  })

  it('desenha a parede e o tapete antes, e os objetos de trás para a frente', () => {
    const placements = roomLayout({
      equipment: { ...STARTER_ROOM, GYM: 3 },
      items: ['rug_round', 'poster_space', 'item_novo'],
      player: dressCharacter([]),
    })
    const order = keys(placements)
    expect(order[0]).toBe('decor-poster_space')
    expect(order[1]).toBe('decor-rug_round')
    expect(order.indexOf('player')).toBeGreaterThan(order.indexOf('equipment-BED'))
    expect(order.indexOf('equipment-GYM')).toBeGreaterThan(order.indexOf('player'))
    expect(order).not.toContain('decor-item_novo')
  })
})
