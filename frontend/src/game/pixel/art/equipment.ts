import type { EquipmentTrack } from '../../../api/types'
import type { Sprite } from '../sprite'
import { FURNITURE } from './furniture'

/*
 * Melhorias do quarto, degrau por degrau (0 = o quarto de quem está começando). O degrau 2 da mesa, da cama e
 * da estante reaproveita os móveis que já existiam. Arte original do GasmTask.
 */

const INK = '#1a1c2c'
const WOOD = '#c98a4b'
const WOOD_DARK = '#a0612e'
const WALNUT = '#8f5a3a'
const WALNUT_DARK = '#6b3e26'
const WHITE = '#f4f4f4'

const BOOKS = { r: '#b13e53', b: '#3b5dc9', g: '#38b764', y: '#ffcd75', p: '#e8558e', o: '#ef7d57', z: WHITE }

const pad = (left: string, width: number) => left.padEnd(width, '.')

// ------------------------------------------------------------------ computador (Projeto)

const PHONE_SETUP: Sprite = {
  rows: ['.kkk..............', '.kck..............', '.kck..............', '.kck.kkkkkkkkkk...', 'kkkkkkzgzgzgzgk.kk', '..k..kkkkkkkkkk.kk'],
  palette: { k: INK, c: '#73eff7', z: WHITE, g: '#94b0c2' },
}

const OLD_LAPTOP: Sprite = {
  rows: [
    '.kkkkkkkkkkkk.',
    '.kbbbbbbbbbbk.',
    '.kbccccccccbk.',
    '.kbcllcccccbk.',
    '.kbccccccccbk.',
    '.kbbbbbbbbbbk.',
    'kkkkkkkkkkkkkk',
    'kBBBBBBBBBBBBk',
    'kkkkkkkkkkkkkk',
  ],
  palette: { k: INK, b: '#d8c8a8', B: '#b8a888', c: '#7f9c94', l: '#c2e0d0' },
}

const NEW_LAPTOP: Sprite = {
  rows: [
    '.kkkkkkkkkkkk.',
    '.kcccccccccck.',
    '.kczcccccccck.',
    '.kcccccccccck.',
    '.kcccccccccck.',
    '.kkkkkkkkkkkk.',
    'kssssssssssssk',
    'kSSSSSSSSSSSSk',
    '.kkkkkkkkkkkk.',
  ],
  palette: { k: INK, c: '#41a6f6', z: WHITE, s: '#c2cddb', S: '#94b0c2' },
}

const DESKTOP: Sprite = {
  rows: [
    'kkkkkkkkkkkkkk..kkkkkk',
    'kcccccccccccck..kGGGGk',
    'kczzccccccccck..kGpGGk',
    'kcllllllccccck..kGpGGk',
    'kcccllllllccck..kGpGGk',
    'kcllllccccccck..kGpGGk',
    'kcccccccccccck..kGGGGk',
    'kkkkkkkkkkkkkk..kGzGGk',
    '......kk........kGGGGk',
    '.....kkkk.......kGGGGk',
    'kkkkkkkkkkkkkk..kGGGGk',
    'kzgzgzgzgzgzgk..kkkkkk',
    'kkkkkkkkkkkkkk........',
  ],
  palette: { k: INK, c: '#29366f', z: WHITE, l: '#a7f070', G: '#333c57', p: '#8d5ad1', g: '#94b0c2' },
}

// ------------------------------------------------------------------ mesa (Estudo)

const FOLDING_TABLE: Sprite = {
  rows: [
    'kkkkkkkkkkkkkkkkkkkk',
    'kwwwwwwwwwwwwwwwwwwk',
    'kkkkkkkkkkkkkkkkkkkk',
    '..kGk..........kGk..',
    '...kGk........kGk...',
    '....kGk......kGk....',
    '.....kGk....kGk.....',
    '......kGkkkkGk......',
    '.....kGk....kGk.....',
    '....kkk......kkk....',
  ],
  palette: { k: INK, w: '#dcd2c0', G: '#566c86' },
}

const L_DESK: Sprite = {
  rows: [
    'k'.repeat(30),
    `k${'w'.repeat(28)}k`,
    `k${'W'.repeat(28)}k`,
    'k'.repeat(30),
    ...['.kwwwwwk', '.kwwyywk', '.kwwwwwk', '.kkkkkkk', '.kwwwwwk', '.kwwyywk', '.kwwwwwk', '.kkkkkkk', '.kWWWWWk', '.kkkkkkk'].map(
      (drawer) => `${drawer}${'.'.repeat(14)}${[...drawer].reverse().join('')}`,
    ),
  ],
  palette: { k: INK, w: WALNUT, W: WALNUT_DARK, y: '#ffcd75' },
}

// ------------------------------------------------------------------ cama (Sono)

const MATTRESS: Sprite = {
  rows: [
    pad('.kkkkkk', 26),
    `kzzzzzz${'k'.repeat(18)}.`,
    `kzzzzzzk${'b'.repeat(17)}k`,
    'kkkkkkkkbBbbbbBbbbbBbbbbbk',
    `k${'m'.repeat(24)}k`,
    'k'.repeat(26),
  ],
  palette: { k: INK, z: WHITE, b: '#566c86', B: '#94b0c2', m: '#dfe6fb' },
}

const SINGLE_BED: Sprite = {
  rows: [
    pad('kkk', 26),
    pad('kGk', 26),
    pad('kGk', 26),
    pad('kGkkkkkkk', 26),
    `kGkkzzzzk${'k'.repeat(17)}`,
    `kGkkzzzzk${'b'.repeat(16)}k`,
    `kGkkkkkkk${'b'.repeat(16)}k`,
    `kGkmmmmmm${'b'.repeat(16)}k`,
    `kGk${'k'.repeat(23)}`,
    `kGk${'G'.repeat(22)}k`,
    `kGk${'.'.repeat(20)}kGk`,
    `kkk${'.'.repeat(20)}kkk`,
  ],
  palette: { k: INK, G: '#566c86', z: WHITE, b: '#41a6f6', m: '#dfe6fb' },
}

const HEADBOARD_BED: Sprite = {
  rows: [
    pad('kkkkk', 30),
    pad('khhhk', 30),
    pad('khHhk', 30),
    pad('khhhk', 30),
    pad('khHhkkkkkkkk', 30),
    pad('khhhkkzzzzzk', 30),
    `khHhkkzzzzzk${'k'.repeat(18)}`,
    `khhhkkzzzzzk${'d'.repeat(17)}k`,
    `khHhkkkkkkkkdDddDddDddDddDdddk`,
    `khhhksssssss${'d'.repeat(17)}k`,
    `khHhk${'k'.repeat(25)}`,
    `khhhk${'w'.repeat(24)}k`,
    `khhhk${'W'.repeat(24)}k`,
    'k'.repeat(30),
    `.kWk${'.'.repeat(22)}kWk.`,
    `.kkk${'.'.repeat(22)}kkk.`,
  ],
  palette: { k: INK, h: '#566c86', H: '#333c57', z: WHITE, d: '#e3e9f2', D: '#c2cddb', s: WHITE, w: WALNUT, W: WALNUT_DARK },
}

// ------------------------------------------------------------------ estante (Leitura)

const BOOK_PILE: Sprite = {
  rows: ['..kkkkkk..', '..krrrrk..', '.kkkkkkkk.', '.kbbbbbbk.', 'kkkkkkkkkk', 'kggggggggk', 'kkkkkkkkkk'],
  palette: { k: INK, ...BOOKS },
}

const WALL_SHELF: Sprite = {
  rows: [
    '..rr.......gg.....',
    '..rrbb.....gg..pp.',
    '..rrbbyy...ggo.pp.',
    '..rrbbyy..zggoopp.',
    '..rrbbyy.zzggoopp.',
    '..rrbbyyzzzggoopp.',
    'k'.repeat(18),
    `k${'w'.repeat(16)}k`,
    'k'.repeat(18),
    '.kWk..........kWk.',
    '..kk..........kk..',
  ],
  palette: { k: INK, w: WOOD, W: WOOD_DARK, ...BOOKS },
}

const DOUBLE_SHELVES: [string[], string[]][] = [
  [
    ['rr..g..pp.', 'rrbbg..pp.', 'rrbbgyypp.', 'rrbbgyyppo', 'rrbbgyyppo'],
    ['..zz..bbb.', '.kzz..bbb.', 'gkzzr.bbbg', 'gkzzr.bbbg', 'gkzzrrbbbg'],
  ],
  [
    ['b.........', 'bb....gg..', 'bbyy..gg..', 'bbyyp.ggrr', 'bbyyppggrr'],
    ['....GG....', '...GGGG...', '....GG....', '...kkkk...', '...kook...'],
  ],
  [
    ['...yy.....', '..yYYy....', '...yy..pp.', '..kkkk.pp.', '..kWWk.pp.'],
    ['oo..rr....', 'oo..rrbb..', 'oogg.rbb..', 'ooggrrbbzz', 'ooggrrbbzz'],
  ],
  [
    ['gg..bb..rr', 'gg..bb..rr', 'ggzzbbyyrr', 'ggzzbbyyrr', 'ggzzbbyyrr'],
    ['..........', 'kkkkkkkk..', 'kzzzzzzk..', 'kzzzzzzk..', 'kkkkkkkk..'],
  ],
]

const DOUBLE_BOOKSHELF: Sprite = {
  rows: [
    'k'.repeat(26),
    `k${'w'.repeat(24)}k`,
    'k'.repeat(26),
    ...DOUBLE_SHELVES.flatMap(([left, right]) => [
      ...left.map((row, index) => `kW${row}WW${right[index]}Wk`),
      `k${'W'.repeat(24)}k`,
    ]),
    'k'.repeat(26),
    `.kk${'.'.repeat(20)}kk.`,
  ],
  palette: { k: INK, w: WOOD, W: WOOD_DARK, G: '#a7f070', Y: '#e8a33d', ...BOOKS },
}

// ------------------------------------------------------------------ treino (Exercício)

const DUMBBELLS: Sprite = {
  rows: ['.kk...kk.kk...kk.', '.kGkkkGk.kGkkkGk.', '.kk...kk.kk...kk.', 'a'.repeat(17), 'A'.repeat(17)],
  palette: { k: INK, G: '#566c86', a: '#38b764', A: '#257179' },
}

const BENCH: Sprite = {
  rows: [
    pad(`.${'k'.repeat(17)}`, 24),
    pad(`.k${'p'.repeat(15)}k`, 24),
    pad(`.k${'P'.repeat(15)}k`, 24),
    pad(`.${'k'.repeat(17)}`, 24),
    pad('...kGk.......kGk', 24),
    pad('...kGk.......kGk', 24),
    '...kGk.......kGk.kk...kk',
    '..kkkkk.....kkkkkkGkkkGk',
    `${'.'.repeat(17)}kk...kk`,
  ],
  palette: { k: INK, p: '#b13e53', P: '#8a2238', G: '#566c86' },
}

const RACK_GAP = '.'.repeat(16)
const RACK: Sprite = {
  rows: [
    `..kkk${RACK_GAP}kkk..`,
    `..kGk${RACK_GAP}kGk..`,
    `..kGk${RACK_GAP}kGk..`,
    `kkkGk${RACK_GAP}kGkkk`,
    `rrkGk${RACK_GAP}kGkrr`,
    `rrkGk${RACK_GAP}kGkrr`,
    `rr${'z'.repeat(22)}rr`,
    `rrkGk${RACK_GAP}kGkrr`,
    `rrkGk${RACK_GAP}kGkrr`,
    `kkkGk${RACK_GAP}kGkkk`,
    ...Array.from({ length: 6 }, () => `..kGk${RACK_GAP}kGk..`),
    '..kGk......kkkkkk....kGk..',
    '..kGk......krrrrk....kGk..',
    '..kGk......kkkkkk....kGk..',
    '..kGk......kGGGGk....kGk..',
    '.kkkkk.....kkkkkk....kkkkk',
  ],
  palette: { k: INK, G: '#566c86', r: '#b13e53', z: '#c2cddb' },
}

// ------------------------------------------------------------------ cantinho de paz (Espiritualidade)

const CUSHION: Sprite = {
  rows: ['..kkkkkk..', '.kppppppk.', 'kpPppppPpk', '.kkkkkkkk.'],
  palette: { k: INK, p: '#c97b5a', P: '#9c5a3e' },
}

const CANDLE_MAT: Sprite = {
  rows: [
    pad('..............y', 18),
    pad('.............yoy', 18),
    '..kkkkkk.....kzk..',
    '.kppppppk....kzk..',
    'kpPppppPpk...kzk..',
    'm'.repeat(18),
    'M'.repeat(18),
  ],
  palette: { k: INK, p: '#c97b5a', P: '#9c5a3e', y: '#ffcd75', o: '#ef7d57', z: WHITE, m: '#a7c4a0', M: '#7a9a74' },
}

const PEACE_CORNER: Sprite = {
  rows: [
    pad('...........kkk', 20),
    pad('..........kyyyk', 20),
    pad('..........kyYyk', 20),
    pad('..........kyyyk', 20),
    pad('...........kkk', 20),
    pad('............k', 20),
    pad('.gg.........k', 20),
    'gGGg........k...gg..',
    '.gGg........k..gGGg.',
    '.kkk..kkkkkkkk..kkk.',
    '.kok..kwwwwwwk..kok.',
    '.kok..kkkkkkkk..kok.',
    '.kkk...k....k...kkk.',
    'm'.repeat(20),
    'M'.repeat(20),
  ],
  palette: {
    k: INK,
    y: '#ffe6a8',
    Y: '#ffcd75',
    g: '#38b764',
    G: '#a7f070',
    o: '#ef7d57',
    w: WOOD,
    m: '#a7c4a0',
    M: '#7a9a74',
  },
}

// ------------------------------------------------------------------ organização (Casa e Outros)

const BOXES: Sprite = {
  rows: [
    '...kkkkkkkk...',
    '...kcccccck...',
    '...kcCCCCck...',
    '...kcccccck...',
    'k'.repeat(14),
    `k${'cccccTTccccc'}k`,
    `k${'c'.repeat(12)}k`,
    `k${'cccCCCcccccc'}k`,
    'k'.repeat(14),
  ],
  palette: { k: INK, c: WOOD, C: WHITE, T: '#e0bd84' },
}

const DRESSER_DRAWER = ['kwwwwwwwwwwwwwwk', `k${'wwwwwwyywwwwww'}k`, 'k'.repeat(16)]
const DRESSER: Sprite = {
  rows: [
    'k'.repeat(16),
    `k${'W'.repeat(14)}k`,
    'k'.repeat(16),
    ...DRESSER_DRAWER,
    ...DRESSER_DRAWER,
    ...DRESSER_DRAWER,
    '.kWk........kWk.',
    '.kkk........kkk.',
  ],
  palette: { k: INK, w: '#e9e2d0', W: '#b8ad96', y: WOOD_DARK },
}

const WARDROBE_PLAIN = 'kwwwwwwwkkwwwwwwwk'
const WARDROBE: Sprite = {
  rows: [
    'k'.repeat(18),
    `k${'w'.repeat(16)}k`,
    'k'.repeat(18),
    WARDROBE_PLAIN,
    WARDROBE_PLAIN,
    'kwzzzzzwkkwwwwwwwk',
    'kwzZzzzwkkwwwwwwwk',
    'kwzzZzzwkkwwwwwwwk',
    'kwzzzZzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzykkywwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    'kwzzzzzwkkwwwwwwwk',
    WARDROBE_PLAIN,
    WARDROBE_PLAIN,
    WARDROBE_PLAIN,
    'k'.repeat(18),
    `k${'W'.repeat(16)}k`,
    'k'.repeat(18),
    `.kk${'.'.repeat(12)}kk.`,
  ],
  palette: { k: INK, w: WALNUT, W: WALNUT_DARK, z: '#c2e8f2', Z: WHITE, y: '#ffcd75' },
}

/** O desenho de cada degrau de cada trilha; `null` é degrau sem nada no quarto. */
export const EQUIPMENT_ART: Record<EquipmentTrack, readonly (Sprite | null)[]> = {
  COMPUTER: [PHONE_SETUP, OLD_LAPTOP, NEW_LAPTOP, DESKTOP],
  DESK: [null, FOLDING_TABLE, FURNITURE.desk_simple, L_DESK],
  BED: [MATTRESS, SINGLE_BED, FURNITURE.bed_cozy, HEADBOARD_BED],
  BOOKSHELF: [BOOK_PILE, WALL_SHELF, FURNITURE.bookshelf_wood, DOUBLE_BOOKSHELF],
  GYM: [null, DUMBBELLS, BENCH, RACK],
  PEACE: [null, CUSHION, CANDLE_MAT, PEACE_CORNER],
  ORGANIZER: [null, BOXES, DRESSER, WARDROBE],
}

/** Código do item da loja de cada degrau (1 a 3), para achar o desenho de um item pelo código. */
export const EQUIPMENT_CODES: Record<EquipmentTrack, readonly string[]> = {
  COMPUTER: ['computer_old_laptop', 'computer_new_laptop', 'computer_desktop'],
  DESK: ['desk_folding', 'desk_simple', 'desk_l_shaped'],
  BED: ['bed_single', 'bed_cozy', 'bed_headboard'],
  BOOKSHELF: ['shelf_wall', 'bookshelf_wood', 'bookshelf_double'],
  GYM: ['gym_dumbbells', 'gym_bench', 'gym_rack'],
  PEACE: ['peace_cushion', 'peace_mat', 'peace_corner'],
  ORGANIZER: ['organizer_boxes', 'organizer_dresser', 'organizer_wardrobe'],
}

export function equipmentSprite(code: string): Sprite | null {
  for (const [track, codes] of Object.entries(EQUIPMENT_CODES) as [EquipmentTrack, readonly string[]][]) {
    const index = codes.indexOf(code)
    if (index >= 0) return EQUIPMENT_ART[track][index + 1]
  }
  return null
}
