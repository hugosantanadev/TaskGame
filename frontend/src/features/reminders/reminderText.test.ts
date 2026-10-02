import { describe, expect, it } from 'vitest'

import type { UpcomingReminder } from '../../api/types'
import { reminderText } from './reminderText'

const SAO_PAULO = 'America/Sao_Paulo'

function reminder(overrides: Partial<UpcomingReminder>): UpcomingReminder {
  return {
    kind: 'TASK',
    notifyAt: '2026-10-02T23:50:00Z',
    eventAt: '2026-10-03T00:00:00Z',
    occurrenceId: 'a',
    title: 'Bíblia',
    ...overrides,
  }
}

describe('texto dos lembretes', () => {
  it('tarefa com antecedência mostra o horário de início no fuso do usuário', () => {
    expect(reminderText(reminder({}), SAO_PAULO)).toEqual({
      title: 'Daqui a pouco: Bíblia',
      body: 'Começa às 21:00.',
    })
  })

  it('sem antecedência, o aviso é "agora"', () => {
    const now = '2026-10-03T00:00:00Z'
    expect(reminderText(reminder({ notifyAt: now, eventAt: now }), SAO_PAULO).title).toBe('Agora: Bíblia')
  })

  it('dormir e acordar têm texto próprio', () => {
    const bedtime = reminder({ kind: 'BEDTIME', title: null, occurrenceId: null, eventAt: '2026-10-03T02:00:00Z' })
    expect(reminderText(bedtime, SAO_PAULO).body).toContain('23:00')
    expect(reminderText(reminder({ kind: 'WAKE_UP', title: null }), SAO_PAULO).title).toBe('Bom dia! Hora de acordar')
  })
})
