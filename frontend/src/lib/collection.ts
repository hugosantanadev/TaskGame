import type { CharacterSlot, CharacterState, StoreItem, StoreItemCategory } from '../api/types'
import { TRACK_LABEL } from './equipment'

export const ITEM_CATEGORY_LABEL: Record<StoreItemCategory, string> = {
  FURNITURE: 'Móveis',
  DECORATION: 'Decoração',
  CHARACTER: 'Personagem',
  EQUIPMENT: 'Melhoria do quarto',
}

export const SLOT_LABEL: Record<CharacterSlot, string> = {
  HEAD: 'Cabeça',
  OUTFIT: 'Roupa',
  ACCESSORY: 'Acessório',
}

export const STATE_LABEL: Record<CharacterState, string> = {
  IDLE: 'Livre',
  STUDYING: 'Estudando',
  AT_COMPUTER: 'No computador',
  READING: 'Lendo',
  SLEEPING: 'Dormindo',
}

/** "Móveis", "Personagem, cabeça" ou "Melhoria do quarto, mesa". */
export function itemKind(item: StoreItem): string {
  if (item.track) return `${ITEM_CATEGORY_LABEL.EQUIPMENT}, ${TRACK_LABEL[item.track].toLowerCase()}`
  return item.slot ? `${ITEM_CATEGORY_LABEL[item.category]}, ${SLOT_LABEL[item.slot].toLowerCase()}` : ITEM_CATEGORY_LABEL[item.category]
}
