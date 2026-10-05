import type { EquipmentStatus, EquipmentTrack, TaskCategory } from '../api/types'
import { categoryLabel } from './categories'
import { listJoin, plural } from './days'

export const TRACK_ORDER: EquipmentTrack[] = ['COMPUTER', 'DESK', 'BED', 'BOOKSHELF', 'GYM', 'PEACE', 'ORGANIZER']

export const TRACK_LABEL: Record<EquipmentTrack, string> = {
  COMPUTER: 'Computador',
  DESK: 'Mesa',
  BED: 'Cama',
  BOOKSHELF: 'Estante',
  GYM: 'Treino',
  PEACE: 'Cantinho de paz',
  ORGANIZER: 'Organização',
}

/** O que existe no degrau zero, antes da primeira compra. */
export const STARTER_LABEL: Record<EquipmentTrack, string> = {
  COMPUTER: 'Celular com teclado e mouse',
  DESK: 'Nada ainda: estudo no chão',
  BED: 'Colchão no chão',
  BOOKSHELF: 'Pilha de livros no chão',
  GYM: 'Nada ainda',
  PEACE: 'Nada ainda',
  ORGANIZER: 'Nada ainda',
}

/** Todas as trilhas no degrau zero: o quarto do primeiro dia. */
export const STARTER_ROOM: Record<EquipmentTrack, number> = {
  COMPUTER: 0,
  DESK: 0,
  BED: 0,
  BOOKSHELF: 0,
  GYM: 0,
  PEACE: 0,
  ORGANIZER: 0,
}

export function tiersOf(statuses: readonly EquipmentStatus[] | undefined): Record<EquipmentTrack, number> {
  const tiers = { ...STARTER_ROOM }
  statuses?.forEach((status) => {
    tiers[status.track] = status.tier
  })
  return tiers
}

/** "+2 moedas por tarefa de Estudo" */
export function bonusText(bonus: number, categories: readonly TaskCategory[]): string {
  return `+${plural(bonus, 'moeda', 'moedas')} por tarefa de ${listJoin(categories.map(categoryLabel))}`
}
