import type { CharacterSlot, CharacterState, StoreItem, StoreItemCategory } from '../api/types'

export const ITEM_CATEGORY_LABEL: Record<StoreItemCategory, string> = {
  FURNITURE: 'Móveis',
  DECORATION: 'Decoração',
  CHARACTER: 'Personagem',
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

/** "Móveis" ou, para roupas e acessórios, "Personagem, cabeça". */
export function itemKind(item: StoreItem): string {
  return item.slot ? `${ITEM_CATEGORY_LABEL[item.category]}, ${SLOT_LABEL[item.slot].toLowerCase()}` : ITEM_CATEGORY_LABEL[item.category]
}
