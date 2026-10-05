import type { MissionSummary } from '../../api/types'

/** Academia e estudo vêm primeiro: são as missões que mais pedem um acompanhamento de perto. */
const FOCUS = new Set(['EXERCISE', 'STUDY'])

/** A missão aberta de início: a de academia ou estudo mais feita, entre as ativas; senão, a mais feita. */
export function defaultMission(missions: readonly MissionSummary[]): MissionSummary | null {
  const active = missions.filter((mission) => !mission.archived)
  const pool = active.length > 0 ? active : missions
  const byCompleted = [...pool].sort((a, b) => b.completed - a.completed)
  return byCompleted.find((mission) => FOCUS.has(mission.category)) ?? byCompleted[0] ?? null
}
