import { describe, expect, it } from 'vitest'

import { CHARACTER_HEIGHT, CHARACTER_WIDTH, WEARABLES, dressCharacter } from './art/character'
import { FURNITURE } from './art/furniture'
import { BOOK, CLOUD, ICONS, LAPTOP, MOON, SUN, TWINKLE, chestSprite, windowSprite } from './art/scenery'
import { mirror, recolor, spriteSize, stack, toRuns, type Sprite } from './sprite'

/** Toda linha com a mesma largura e toda letra com cor na paleta: o desenho não sai torto nem com buraco. */
function expectWellFormed(name: string, sprite: Sprite) {
  const { width } = spriteSize(sprite)
  sprite.rows.forEach((row, y) => {
    expect(row.length, `${name}, linha ${y}: "${row}"`).toBe(width)
    for (const key of row) {
      if (key !== '.') expect(sprite.palette[key], `${name}, linha ${y}: letra "${key}" sem cor`).toBeDefined()
    }
  })
}

describe('motor de sprites', () => {
  const sprite: Sprite = { rows: ['aab.', '.bbb'], palette: { a: '#111', b: '#222' } }

  it('junta pixels vizinhos da mesma cor em faixas', () => {
    expect(toRuns(sprite)).toEqual([
      { x: 0, y: 0, width: 2, color: '#111' },
      { x: 2, y: 0, width: 1, color: '#222' },
      { x: 1, y: 1, width: 3, color: '#222' },
    ])
  })

  it('empilha camadas: o pixel de cima cobre o de baixo e o vazio deixa ver', () => {
    const top: Sprite = { rows: ['..c.', 'c...'], palette: { c: '#333' } }
    const runs = toRuns(stack([sprite, top]))
    expect(runs).toContainEqual({ x: 2, y: 0, width: 1, color: '#333' })
    expect(runs).toContainEqual({ x: 0, y: 0, width: 2, color: '#111' })
    expect(runs).toContainEqual({ x: 0, y: 1, width: 1, color: '#333' })
  })

  it('não mistura letras iguais de paletas diferentes', () => {
    const red: Sprite = { rows: ['a.'], palette: { a: 'red' } }
    const blue: Sprite = { rows: ['.a'], palette: { a: 'blue' } }
    expect(toRuns(stack([red, blue])).map((run) => run.color)).toEqual(['red', 'blue'])
  })

  it('troca cores sem mudar o desenho e espelha na horizontal', () => {
    expect(toRuns(recolor(sprite, { a: '#999' }))[0].color).toBe('#999')
    expect(mirror(sprite).rows).toEqual(['.baa', 'bbb.'])
  })
})

describe('arte', () => {
  it('todos os desenhos estão bem formados', () => {
    Object.entries(FURNITURE).forEach(([code, art]) => expectWellFormed(code, art))
    Object.entries(ICONS).forEach(([name, art]) => expectWellFormed(name, art))
    Object.entries({ SUN, MOON, CLOUD, TWINKLE, BOOK, LAPTOP }).forEach(([name, art]) => expectWellFormed(name, art))
    ;(['WOOD', 'SILVER', 'GOLD', 'LEGENDARY'] as const).forEach((look) => {
      expectWellFormed(`baú ${look}`, chestSprite(look))
      expectWellFormed(`baú ${look} aberto`, chestSprite(look, true))
    })
    ;(['MORNING', 'AFTERNOON', 'SUNSET', 'NIGHT'] as const).forEach((period) =>
      expectWellFormed(`janela ${period}`, windowSprite(period)),
    )
  })

  it('cada roupa ocupa a grade inteira do personagem', () => {
    Object.entries(WEARABLES).forEach(([code, wearable]) => {
      expectWellFormed(code, wearable.sprite)
      expect(spriteSize(wearable.sprite)).toEqual({ width: CHARACTER_WIDTH, height: CHARACTER_HEIGHT })
    })
  })

  it('veste o personagem com tudo junto, inclusive a capa por trás', () => {
    const dressed = dressCharacter(['rank_master_cape', 'hoodie_purple', 'rank_legend_crown'])
    expectWellFormed('personagem vestido', dressed)
    expect(spriteSize(dressed)).toEqual({ width: CHARACTER_WIDTH, height: CHARACTER_HEIGHT })
    // A capa aparece nas laterais, onde o corpo não cobre
    expect(dressed.palette[dressed.rows[16][0]]).toBe('#1a1c2c')
    expect(dressed.palette[dressed.rows[16][1]]).toBe('#b13e53')
  })

  it('ignora códigos sem desenho', () => {
    expect(dressCharacter(['item_que_nao_existe'])).toEqual(dressCharacter([]))
  })
})
