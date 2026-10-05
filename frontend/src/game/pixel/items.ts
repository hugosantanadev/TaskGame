import type { CharacterView } from '../../api/types'
import { dressCharacter, isWearable } from './art/character'
import { FURNITURE } from './art/furniture'
import type { Sprite } from './sprite'

/** Desenho de um item da loja: o móvel em si ou, para roupas, o personagem vestindo só aquela peça. */
export function itemSprite(code: string): Sprite | null {
  if (code in FURNITURE) return FURNITURE[code]
  if (isWearable(code)) return dressCharacter([code])
  return null
}

/** O que o personagem veste agora, a partir da resposta de GET /character. */
export function wornCodes(character: CharacterView | undefined): string[] {
  return character?.slots.flatMap(({ item }) => (item ? [item.item.code] : [])) ?? []
}
