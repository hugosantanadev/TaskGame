import { describe, expect, it } from 'vitest'

import {
  addDays,
  dayOfWeekOf,
  formatDay,
  formatDayMonth,
  formatMonth,
  formatTime,
  listJoin,
  plural,
  todayIso,
  weekStartOf,
} from './days'

describe('datas do plano', () => {
  it('a semana começa na segunda, inclusive quando o dia é domingo', () => {
    expect(weekStartOf('2026-10-04')).toBe('2026-09-28') // domingo
    expect(weekStartOf('2026-09-28')).toBe('2026-09-28') // a própria segunda
    expect(dayOfWeekOf('2026-10-04')).toBe('SUNDAY')
  })

  it('soma dias atravessando mês e ano', () => {
    expect(addDays('2026-09-30', 1)).toBe('2026-10-01')
    expect(addDays('2026-12-31', 1)).toBe('2027-01-01')
    expect(addDays('2026-03-01', -1)).toBe('2026-02-28')
  })

  it('hoje é o do fuso do usuário, não o do aparelho', () => {
    // 02:00 em UTC ainda é o dia anterior em São Paulo (UTC-3)
    const instant = new Date('2026-10-02T02:00:00Z')
    expect(todayIso('America/Sao_Paulo', instant)).toBe('2026-10-01')
    expect(todayIso('Asia/Tokyo', instant)).toBe('2026-10-02')
  })

  it('formata datas e horários como o app mostra', () => {
    expect(formatDay('2026-09-30')).toBe('qua, 30 set')
    expect(formatDayMonth('2026-09-28')).toBe('28/9')
    expect(formatMonth('2026-03-01')).toBe('março de 2026')
    expect(formatTime('08:05:00')).toBe('08:05')
    expect(formatTime(null)).toBeNull()
  })

  it('plural e listas em português', () => {
    expect(plural(1, 'dia', 'dias')).toBe('1 dia')
    expect(plural(0, 'dia', 'dias')).toBe('0 dias')
    expect(listJoin(['seg'])).toBe('seg')
    expect(listJoin(['seg', 'qua', 'sex'])).toBe('seg, qua e sex')
  })
})
