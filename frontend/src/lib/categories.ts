import type { TaskCategory } from '../api/types'

export const CATEGORIES: ReadonlyArray<{ id: TaskCategory; label: string }> = [
  { id: 'STUDY', label: 'Estudo' },
  { id: 'READING', label: 'Leitura' },
  { id: 'SPIRITUALITY', label: 'Espiritualidade' },
  { id: 'EXERCISE', label: 'Exercício' },
  { id: 'SLEEP', label: 'Sono' },
  { id: 'PROJECT', label: 'Projeto' },
  { id: 'HOME', label: 'Casa' },
  { id: 'OTHER', label: 'Outros' },
]

export function categoryLabel(id: TaskCategory): string {
  return CATEGORIES.find((category) => category.id === id)?.label ?? id
}

/** Cor neon da categoria (tokens.css), usada como fita na borda da tarefa. */
export function categoryColor(id: TaskCategory): string {
  return `var(--cat-${id.toLowerCase()})`
}
