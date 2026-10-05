import type { Sprite } from '../sprite'

/*
 * Marca do GasmTask: o rosto do mascote (o mesmo personagem do jogo, de camiseta azul). Este arquivo não importa
 * nada em tempo de execução: o script dos ícones (scripts/generate-icons.ts) o lê direto com o Node.
 */
export const MASCOT: Sprite = {
  rows: [
    '....kkkkkkkk....',
    '...khhhhhhhhk...',
    '..khhhhhhhhhhk..',
    '..khhhhhhhhhhk..',
    '..khhhssssshhk..',
    '..khsssssssshk..',
    '..khsksssskshk..',
    '..khsksssskshk..',
    '..kscsssssscsk..',
    '..kssssmmssssk..',
    '...kkkkkkkkkk...',
    '...kttttttttk...',
    '..kttttttttttk..',
  ],
  palette: {
    k: '#1a1c2c',
    h: '#6b3e26',
    s: '#f6c69e',
    c: '#f39a8b',
    m: '#b13e53',
    t: '#41a6f6',
  },
}

/** Fundo da marca: o amarelo das moedas. */
export const BRAND_BACKGROUND = '#ffcd75'
