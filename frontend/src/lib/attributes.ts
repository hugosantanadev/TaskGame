import type { Attribute, AttributeStatus } from '../api/types'
import { categoryColor, categoryLabel } from './categories'
import { listJoin } from './days'

export const ATTRIBUTE_LABELS: Record<Attribute, string> = {
  INTELLIGENCE: 'Inteligência',
  STRENGTH: 'Força',
  WISDOM: 'Sabedoria',
  SPIRIT: 'Espírito',
  VITALITY: 'Vitalidade',
  CREATIVITY: 'Criatividade',
  DISCIPLINE: 'Disciplina',
}

/** A cor do atributo é a da categoria que o treina (a mesma fita das tarefas). */
export function attributeColor(status: AttributeStatus): string {
  const [first] = status.categories
  return first ? categoryColor(first) : 'var(--pen)'
}

/** "Estudo", "Casa e Outros" */
export function trainedBy(status: AttributeStatus): string {
  return listJoin(status.categories.map(categoryLabel))
}

/** Progresso dentro do nível, de 0 a 1. */
export function levelProgress(status: AttributeStatus): number {
  const span = status.nextLevelXp - status.levelStartXp
  return span <= 0 ? 1 : Math.min(1, Math.max(0, (status.xp - status.levelStartXp) / span))
}
