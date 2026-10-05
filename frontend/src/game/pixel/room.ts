/* Onde cada móvel e decoração fica no quarto (cena de 96 × 64 pixels da arte, piso a partir da linha 44). */

type Spot = { x: number; y: number }

/** Onde cada item fica. A ordem é a do desenho: o que vem depois aparece na frente. */
const SPOTS: [code: string, spot: Spot][] = [
  ['poster_space', { x: 6, y: 7 }],
  ['neon_sign', { x: 62, y: 5 }],
  ['bookshelf_wood', { x: 82, y: 20 }],
  ['aquarium_small', { x: 82, y: 10 }],
  ['bed_cozy', { x: 1, y: 34 }],
  ['desk_simple', { x: 58, y: 33 }],
  ['lamp_desk', { x: 71, y: 23 }],
  ['mug_coffee', { x: 61, y: 25 }],
  ['plant_small', { x: 30, y: 35 }],
  ['rug_round', { x: 32, y: 53 }],
  ['chair_gamer', { x: 63, y: 38 }],
  ['beanbag_purple', { x: 10, y: 50 }],
]

/** Sem a estante ou a escrivaninha, o que ia em cima delas vai para o chão. */
const ON_FLOOR: Record<string, { needs: string; spot: Spot }> = {
  aquarium_small: { needs: 'bookshelf_wood', spot: { x: 82, y: 36 } },
  lamp_desk: { needs: 'desk_simple', spot: { x: 72, y: 36 } },
  mug_coffee: { needs: 'desk_simple', spot: { x: 62, y: 40 } },
}

/** Os itens do quarto com a posição de cada um, na ordem de desenho. */
export function roomLayout(codes: readonly string[]): { code: string; spot: Spot }[] {
  const placed = new Set(codes)
  return SPOTS.filter(([code]) => placed.has(code)).map(([code, spot]) => {
    const fallback = ON_FLOOR[code]
    return { code, spot: fallback && !placed.has(fallback.needs) ? fallback.spot : spot }
  })
}
