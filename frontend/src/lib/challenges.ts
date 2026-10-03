import type { ChallengeCode, DailyChallenge } from '../api/types'

const NAMES: Record<ChallengeCode, string> = {
  EARLY_BIRD: 'Madrugador',
  ON_TIME: 'Pontual',
  PHOTO: 'Registro',
  EXTRA_MILE: 'Milha extra',
  VARIETY: 'Variedade',
  FULL_DAY: 'Dia completo',
  MARATHON: 'Maratona',
}

export function challengeName(code: ChallengeCode): string {
  return NAMES[code]
}

/** O que fazer, com a meta do desafio. */
export function challengeGoal(challenge: Pick<DailyChallenge, 'code' | 'target'>): string {
  switch (challenge.code) {
    case 'EARLY_BIRD':
      return `Conclua ${challenge.target} tarefas antes do meio-dia.`
    case 'ON_TIME':
      return `Conclua ${challenge.target} tarefas no horário.`
    case 'PHOTO':
      return 'Conclua uma tarefa com foto.'
    case 'EXTRA_MILE':
      return 'Conclua uma extra.'
    case 'VARIETY':
      return `Conclua tarefas de ${challenge.target} categorias diferentes.`
    case 'FULL_DAY':
      return 'Conclua todas as obrigatórias de hoje.'
    case 'MARATHON':
      return `Conclua ${challenge.target} tarefas hoje.`
  }
}
