import type { CharacterSlot } from '../../../api/types'
import { stack, type Palette, type Sprite } from '../sprite'

/*
 * Personagem em 16 × 20: cabeça grande (estilo chibi), corpo curto. Cada roupa é uma camada do mesmo tamanho
 * que cobre só os pixels dela. A arte é original do GasmTask; a paleta segue a Sweetie 16.
 */

export const CHARACTER_WIDTH = 16
export const CHARACTER_HEIGHT = 20

const INK = '#1a1c2c'
const SKIN = '#f6c69e'

const BASE_PALETTE: Palette = {
  k: INK,
  h: '#6b3e26', // cabelo
  s: SKIN,
  c: '#f39a8b', // bochecha
  m: '#b13e53', // boca
  t: '#41a6f6', // camiseta padrão
  p: '#333c57', // calça
  o: '#566c86', // tênis
}

/** Corpo sem roupa nova: cabelo castanho, camiseta azul e olhos abertos ou fechados (dormindo). */
function base(sleeping: boolean): Sprite {
  const eyes = sleeping ? ['..khsssssssshk..', '..khkksssskkhk..'] : ['..khsksssskshk..', '..khsksssskshk..']
  return {
    rows: [
      '................',
      '....kkkkkkkk....',
      '...khhhhhhhhk...',
      '..khhhhhhhhhhk..',
      '..khhhhhhhhhhk..',
      '..khhhssssshhk..',
      '..khsssssssshk..',
      ...eyes,
      '..kscsssssscsk..',
      '..kssssmmssssk..',
      '...kkkkkkkkkk...',
      '...kttttttttk...',
      '..kttttttttttk..',
      '..kttttttttttk..',
      '..kskttttttksk..',
      '....kppppppk....',
      '....kppkkppk....',
      '....kookkook....',
      '....kkkkkkkk....',
    ],
    palette: BASE_PALETTE,
  }
}

/** Camada que começa na linha `top` da grade do personagem; o resto fica vazio. */
function layer(top: number, rows: readonly string[], palette: Palette): Sprite {
  const empty = '.'.repeat(CHARACTER_WIDTH)
  return {
    rows: Array.from({ length: CHARACTER_HEIGHT }, (_, y) => rows[y - top] ?? empty),
    palette: { k: INK, s: SKIN, ...palette },
  }
}

type Wearable = { slot: CharacterSlot; sprite: Sprite; behind?: boolean }

/** Roupas por código do item da loja (inclusive as recompensas de elo). */
export const WEARABLES: Record<string, Wearable> = {
  cap_red: {
    slot: 'HEAD',
    sprite: layer(
      0,
      ['....kkkkkkkk....', '...krrrrrrrrk...', '..krrrrwwrrrrk..', '..krrrrrrrrrrk..', '..kRRRRRRRRRRRRk'],
      { r: '#d6455b', R: '#8a2238', w: '#f4f4f4' },
    ),
  },
  headphones_basic: {
    slot: 'HEAD',
    sprite: layer(
      0,
      [
        '...kkkkkkkkkk...',
        '..kggggggggggk..',
        '.kgk........kgk.',
        'kgk..........kgk',
        'kgk..........kgk',
        'kkkk........kkkk',
        'kaak........kaak',
        'kaak........kaak',
        'kkkk........kkkk',
      ],
      { g: '#333c57', a: '#ef7d57' },
    ),
  },
  rank_bronze_headband: {
    slot: 'HEAD',
    sprite: layer(4, ['..kbbbbbbbbbbbk.', '............kbbk', '.............kBk'], { b: '#cd7f4a', B: '#8f4f26' }),
  },
  rank_platinum_visor: {
    slot: 'HEAD',
    sprite: layer(
      6,
      ['.kkkkkkkkkkkkkk.', '.kvvvvvvvvvvvvk.', '.kvwwvvvvvvvvvk.', '.kkkkkkkkkkkkkk.'],
      { v: '#73eff7', w: '#f4f4f4' },
    ),
  },
  rank_legend_crown: {
    slot: 'HEAD',
    sprite: layer(
      0,
      [
        '..k....kk....k..',
        '..kk..kyyk..kk..',
        '..kyk.kyyk.kyk..',
        '..kyykkyykkyyk..',
        '..kyyyyyyyyyyk..',
        '..kyryybyyyryk..',
        '..kkkkkkkkkkkk..',
      ],
      { y: '#ffcd75', r: '#b13e53', b: '#41a6f6' },
    ),
  },
  tshirt_stripes: {
    slot: 'OUTFIT',
    sprite: layer(
      12,
      ['...kooooooook...', '..kyyyyyyyyyyk..', '..kggggggggggk..', '..kskbbbbbbksk..'],
      { o: '#ef7d57', y: '#ffcd75', g: '#38b764', b: '#41a6f6' },
    ),
  },
  hoodie_purple: {
    slot: 'OUTFIT',
    sprite: layer(
      12,
      ['...kmmwmmwmmk...', '..kmmmwmmwmmmk..', '..kmmmmmmmmmmk..', '..kskmMMMMmksk..'],
      { m: '#8d5ad1', M: '#5d3a9b', w: '#f4f4f4' },
    ),
  },
  rank_silver_jacket: {
    slot: 'OUTFIT',
    sprite: layer(
      12,
      ['...klllttlllk...', '..klllLttLlllk..', '..klllLttLlllk..', '..kskllttllksk..'],
      { l: '#c2cddb', L: '#566c86', t: '#41a6f6' },
    ),
  },
  rank_diamond_armor: {
    slot: 'OUTFIT',
    sprite: layer(
      12,
      ['..kDDddddddDDk..', '..kDddddddddDk..', '..kDdddwwdddDk..', '..kskDkkkkDksk..'],
      { d: '#73eff7', D: '#41a6f6', w: '#f4f4f4' },
    ),
  },
  scarf_knit: {
    slot: 'ACCESSORY',
    sprite: layer(
      11,
      ['..kffffffffffk..', '..kfFfFfFfFffk..', '.........kfk....', '.........kFk....', '.........kkk....'],
      { f: '#e8558e', F: '#b13e53' },
    ),
  },
  glasses_round: {
    slot: 'ACCESSORY',
    sprite: layer(6, ['....kkk..kkk....', '..kkk.kkkk.kkk..', '....k.k..k.k....', '....kkk..kkk....'], {
      k: '#333c57',
    }),
  },
  rank_gold_chain: {
    slot: 'ACCESSORY',
    sprite: layer(12, ['.....y....y.....', '......y..y......', '......kyyk......', '......kYYk......'], {
      y: '#ffcd75',
      Y: '#e8a33d',
    }),
  },
  rank_master_cape: {
    slot: 'ACCESSORY',
    behind: true,
    sprite: layer(
      11,
      [
        '.kcccccccccccck.',
        '.kcccccccccccck.',
        'kcccccccccccccck',
        'kcccccccccccccck',
        'kcccccccccccccck',
        'kcccccccccccccck',
        'kcccccccccccccck',
        'kCCCCCCCCCCCCCCk',
        'kkkkkkkkkkkkkkkk',
      ],
      { c: '#b13e53', C: '#ffcd75' },
    ),
  },
}

export function isWearable(code: string): boolean {
  return code in WEARABLES
}

/**
 * Monta o personagem com o que ele veste: capa por trás, corpo, roupa, acessório e, por cima, o que vai na
 * cabeça. Códigos sem desenho são ignorados (o personagem aparece sem aquela peça).
 */
export function dressCharacter(codes: readonly string[], options: { sleeping?: boolean } = {}): Sprite {
  const worn = codes.map((code) => WEARABLES[code]).filter((item): item is Wearable => item !== undefined)
  const order: CharacterSlot[] = ['OUTFIT', 'ACCESSORY', 'HEAD']
  return stack([
    ...worn.filter((item) => item.behind).map((item) => item.sprite),
    base(options.sleeping ?? false),
    ...order.flatMap((slot) => worn.filter((item) => item.slot === slot && !item.behind).map((item) => item.sprite)),
  ])
}
