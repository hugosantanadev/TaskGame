import type { Attribute, TitleCode, TitleStatus } from '../api/types'

/**
 * Nomes dos títulos (o backend só manda o código). Três por atributo: nível 3, 6 e 10. O do nível 10 é sempre
 * "Mestre", o topo daquele treino.
 */
export const TITLE_LABELS: Record<TitleCode, string> = {
  STUDENT: 'Estudante',
  SHARP_MIND: 'Mente afiada',
  STUDY_MASTER: 'Mestre nos estudos',
  GYM_REGULAR: 'Treino em dia',
  GYM_RAT: 'Rato de academia',
  STRENGTH_MASTER: 'Mestre da força',
  CURIOUS_READER: 'Leitura curiosa',
  BOOKWORM: 'Devora-livros',
  WISDOM_MASTER: 'Mestre da sabedoria',
  SERENE_SOUL: 'Alma serena',
  STEADY_HEART: 'Coração firme',
  SPIRIT_MASTER: 'Mestre do espírito',
  WELL_RESTED: 'Sono em dia',
  FULL_ENERGY: 'Energia total',
  REST_MASTER: 'Mestre do descanso',
  HANDS_ON: 'Mão na massa',
  BUILDER: 'Fábrica de ideias',
  CREATOR_MASTER: 'Mestre da criação',
  TIDY_HOME: 'Casa em ordem',
  ORGANIZED: 'Tudo no lugar',
  DISCIPLINE_MASTER: 'Mestre da disciplina',
}

export function titleLabel(code: TitleCode): string {
  return TITLE_LABELS[code] ?? code
}

/** "Você é Mestre nos estudos" — a frase da comemoração. */
export function titleSentence(code: TitleCode): string {
  return `Você é ${titleLabel(code)}`
}

/** Mestre é o último título do atributo e ganha a moldura dourada. */
export function isMasterTitle(code: TitleCode): boolean {
  return code.endsWith('_MASTER')
}

/** O próximo título ainda fechado daquele atributo, para mostrar "faltam X níveis". */
export function nextTitle(titles: readonly TitleStatus[], attribute: Attribute): TitleStatus | null {
  return titles.filter((title) => title.attribute === attribute && !title.unlocked).sort((a, b) => a.level - b.level)[0] ?? null
}

/** O maior título já ganho daquele atributo. */
export function bestTitle(titles: readonly TitleStatus[], attribute: Attribute): TitleStatus | null {
  return titles.filter((title) => title.attribute === attribute && title.unlocked).sort((a, b) => b.level - a.level)[0] ?? null
}
