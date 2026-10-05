import type { Period } from '../../../api/types'
import { recolor, stack, type Palette, type Sprite } from '../sprite'

/* Cenário, baús, ícones do placar e objetos que o personagem segura. Arte original do GasmTask. */

const INK = '#1a1c2c'
const WHITE = '#f4f4f4'

// ------------------------------------------------------------------ céu

/** Cor do céu de cada período, a mesma da cena da tela Hoje. */
export const SKY: Record<Period, string> = {
  MORNING: '#ffd98a',
  AFTERNOON: '#8fe4ff',
  SUNSET: '#f79a6e',
  NIGHT: '#29366f',
}

export const SUN: Sprite = {
  rows: [
    '....yyyy....',
    '..yyyyyyyy..',
    '.yyyyyyyyyy.',
    '.yyzzyyyyyy.',
    'yyyzyyyyyyyy',
    'yyyyyyyyyyyy',
    'yyyyyyyyyyyY',
    'yyyyyyyyyyYY',
    '.yyyyyyyyYY.',
    '.yyyyyyyYYY.',
    '..yyyyYYYY..',
    '....YYYY....',
  ],
  palette: { y: '#ffcd75', Y: '#f2a33a', z: '#fff6c2' },
}

/** O sol do fim de tarde, mais baixo e alaranjado. */
export const SUNSET_SUN = recolor(SUN, { y: '#ffb26b', Y: '#ef7d57', z: '#ffe0b8' })

export const MOON: Sprite = {
  rows: [
    '....mmmm....',
    '..mmmmM.....',
    '.mmmmM......',
    '.mmmM.......',
    'mmmmM.......',
    'mmmM........',
    'mmmM........',
    'mmmmM.......',
    '.mmmmM......',
    '.mmmmmM...m.',
    '..mmmmmmmm..',
    '....mmmm....',
  ],
  palette: { m: '#fff6c2', M: '#ffcd75' },
}

export const CLOUD: Sprite = {
  rows: [
    '....kkkk........',
    '..kkwwwwkkk.....',
    '.kwwwwwwwwwkkk..',
    'kwwwwwwwwwwwwwk.',
    'kwwwwwwwwwwwwwwk',
    '.kkkkkkkkkkkkkk.',
  ],
  palette: { k: '#c2cddb', w: '#ffffff' },
}

export const NIGHT_CLOUD = recolor(CLOUD, { k: '#566c86', w: '#3b4a7a' })

export const TWINKLE: Sprite = { rows: ['.z.', 'zzz', '.z.'], palette: { z: '#fff6c2' } }

/** Janela do quarto: o vidro mostra o céu do período, com sol ou lua. */
export function windowSprite(period: Period): Sprite {
  const frame: Sprite = {
    rows: [
      'kkkkkkkkkkkkkkkkkkk',
      'kfffffffffffffffffk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfffffffffffffffffk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfsssssssfsssssssfk',
      'kfffffffffffffffffk',
      'kWWWWWWWWWWWWWWWWWk',
      'kkkkkkkkkkkkkkkkkkk',
    ],
    palette: { k: INK, f: WHITE, s: SKY[period], W: '#a0612e' },
  }
  const night = period === 'NIGHT'
  const extras: Sprite = night
    ? {
        rows: [
          '...................',
          '...................',
          '.............mm....',
          '....z.......mm.....',
          '.............mm....',
          '.......z...........',
          '...................',
          '...................',
          '.............z.....',
          '...z...............',
          '...................',
        ],
        palette: { m: '#fff6c2', z: '#fff6c2' },
      }
    : {
        rows: [
          '...................',
          '...................',
          '..............u....',
          '.............uuu...',
          '..............u....',
          '...................',
          '...................',
          '....cc.............',
          '...cccc............',
          '..cccccc...........',
          '...................',
        ],
        palette: { u: period === 'SUNSET' ? '#ef7d57' : '#ffcd75', c: WHITE },
      }
  return stack([frame, extras])
}

// ------------------------------------------------------------------ baú semanal

export type ChestLook = 'WOOD' | 'SILVER' | 'GOLD' | 'LEGENDARY'

const CHEST_COLORS: Record<ChestLook, Palette> = {
  WOOD: { w: '#c98a4b', W: '#a0612e', b: '#566c86', y: '#ffcd75' },
  SILVER: { w: '#e3e9f2', W: '#94b0c2', b: '#566c86', y: '#73eff7' },
  GOLD: { w: '#ffe08a', W: '#f2a33a', b: '#b4693a', y: '#f4f4f4' },
  LEGENDARY: { w: '#c9a8f0', W: '#8d5ad1', b: '#ffcd75', y: '#73eff7' },
}

const CLOSED_CHEST: readonly string[] = [
  '...kkkkkkkkkk...',
  '..kwwwwwwwwwwk..',
  '.kwwwwwwwwwwwwk.',
  'kwwwwwwwwwwwwwwk',
  'kbbbbbbbbbbbbbbk',
  'kkkkkkkkkkkkkkkk',
  'kWWWWWkkkkWWWWWk',
  'kbbbbbkyykbbbbbk',
  'kWWWWWkkkkWWWWWk',
  'kWWWWWWWWWWWWWWk',
  'kbbbbbbbbbbbbbbk',
  'kWWWWWWWWWWWWWWk',
  'kkkkkkkkkkkkkkkk',
]

const OPEN_CHEST: readonly string[] = [
  '..kkkkkkkkkkkk..',
  '.kbbbbbbbbbbbbk.',
  '.kWWWWWWWWWWWWk.',
  '.kWWWWWWWWWWWWk.',
  '.kbbbbbbbbbbbbk.',
  'kkkkkkkkkkkkkkkk',
  'kgzcggcgzgcgcggk',
  'kcccgcccccgcccck',
  'kkkkkkkkkkkkkkkk',
  'kWWWWWWWWWWWWWWk',
  'kbbbbbkyykbbbbbk',
  'kWWWWWkkkkWWWWWk',
  'kWWWWWWWWWWWWWWk',
  'kbbbbbbbbbbbbbbk',
  'kkkkkkkkkkkkkkkk',
]

export function chestSprite(look: ChestLook, open = false): Sprite {
  return {
    rows: open ? OPEN_CHEST : CLOSED_CHEST,
    palette: { k: INK, g: '#fff6c2', c: '#ffcd75', z: WHITE, ...CHEST_COLORS[look] },
  }
}

// ------------------------------------------------------------------ ícones do placar

export const ICONS = {
  coin: {
    rows: ['..kkkkk..', '.kwyyyyk.', 'kwyyYyyyk', 'kyyyYyyYk', 'kyyyYyyYk', 'kyyyYyyYk', 'kyyyyyyYk', '.kyyyyYk.', '..kkkkk..'],
    palette: { k: INK, y: '#ffcd75', Y: '#e8a33d', w: '#fff6c2' },
  },
  flame: {
    rows: ['...k.....', '..kok....', '..kook.k.', '.kooookok', '.kooyoook', 'kooyyyook', 'kooyyyook', 'koyyzyyok', '.kooyyok.', '..kkkkk..'],
    palette: { k: INK, o: '#ef7d57', y: '#ffcd75', z: '#fff6c2' },
  },
  gem: {
    rows: ['..kkkkk..', '.kxxzxxk.', 'kxxzxxxXk', 'kXxxxxXXk', '.kXxxXXk.', '..kXXXk..', '...kXk...', '....k....'],
    palette: { k: INK, x: '#73eff7', X: '#41a6f6', z: WHITE },
  },
  star: {
    rows: ['....k....', '...kyk...', 'kkkyyykkk', 'kyyyyyyyk', '.kyyyyyk.', '..kyyyk..', '.kyykyyk.', '.kyk.kyk.', '.kk...kk.'],
    palette: { k: INK, y: '#ffcd75' },
  },
  chest: chestSprite('GOLD'),
  shield: {
    rows: ['kkkkkkkkk', 'kbbbbbbBk', 'kbzbbbbBk', 'kbzbbbbBk', 'kbbbbbbBk', '.kbbbbBk.', '.kbbbbBk.', '..kbbBk..', '...kkk...'],
    palette: { k: INK, b: '#73eff7', B: '#41a6f6', z: WHITE },
  },
} satisfies Record<string, Sprite>

export type IconName = keyof typeof ICONS

// ------------------------------------------------------------------ objetos do estado do personagem

export const BOOK: Sprite = {
  rows: ['.kkkk..kkkk.', 'kwwwwkkwwwwk', 'kwggwkkwggwk', 'kwwwwkkwwwwk', 'kwggwkkwggwk', 'kwwwwkkwwwwk', 'kbbbbbbbbbbk', '.kkkkkkkkkk.'],
  palette: { k: INK, w: WHITE, g: '#94b0c2', b: '#b13e53' },
}

export const LAPTOP: Sprite = {
  rows: ['..kkkkkkkk..', '..kcccccck..', '..kczcccck..', '..kcccczck..', '..kkkkkkkk..', '.kggggggggk.', 'kggggggggggk', 'kkkkkkkkkkkk'],
  palette: { k: INK, c: '#73eff7', z: WHITE, g: '#94b0c2' },
}
