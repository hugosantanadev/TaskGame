import { describe, expect, it } from 'vitest'

import type { MissionSummary } from '../../api/types'
import { defaultMission } from './missionPick'

function mission(name: string, category: MissionSummary['category'], completed: number, archived = false): MissionSummary {
  return {
    taskId: name,
    name,
    category,
    archived,
    completed,
    missed: 0,
    completionRate: null,
    currentStreak: 0,
    bestStreak: 0,
    lastCompletedDate: null,
  }
}

describe('missão aberta de início', () => {
  it('prefere academia ou estudo, mesmo que outra missão tenha mais conclusões', () => {
    const missions = [mission('Ler', 'READING', 30), mission('Academia', 'EXERCISE', 12), mission('Java', 'STUDY', 20)]
    expect(defaultMission(missions)?.name).toBe('Java')
  })

  it('deixa as arquivadas de lado enquanto houver ativas', () => {
    const missions = [mission('Academia antiga', 'EXERCISE', 50, true), mission('Ler', 'READING', 3)]
    expect(defaultMission(missions)?.name).toBe('Ler')
  })

  it('sem missões, não escolhe nenhuma', () => {
    expect(defaultMission([])).toBeNull()
  })
})
