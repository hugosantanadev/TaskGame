import type { DayOfWeek } from '../api/types'

/** Datas do plano como texto AAAA-MM-DD (dias de calendário, sem hora nem fuso), igual ao backend. */

export const WEEK_DAYS: ReadonlyArray<{ id: DayOfWeek; short: string; long: string }> = [
  { id: 'MONDAY', short: 'seg', long: 'segunda-feira' },
  { id: 'TUESDAY', short: 'ter', long: 'terça-feira' },
  { id: 'WEDNESDAY', short: 'qua', long: 'quarta-feira' },
  { id: 'THURSDAY', short: 'qui', long: 'quinta-feira' },
  { id: 'FRIDAY', short: 'sex', long: 'sexta-feira' },
  { id: 'SATURDAY', short: 'sáb', long: 'sábado' },
  { id: 'SUNDAY', short: 'dom', long: 'domingo' },
]

const MONTHS = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']

function toUtc(iso: string): Date {
  const [year = 1970, month = 1, day = 1] = iso.split('-').map(Number)
  return new Date(Date.UTC(year, month - 1, day))
}

/** Hoje no fuso do usuário, nunca no do aparelho. */
export function todayIso(timeZone: string, now: Date = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone, year: 'numeric', month: '2-digit', day: '2-digit' }).format(now)
}

export function addDays(iso: string, days: number): string {
  const date = toUtc(iso)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

function mondayIndex(iso: string): number {
  return (toUtc(iso).getUTCDay() + 6) % 7
}

export function dayOfWeekOf(iso: string): DayOfWeek {
  return WEEK_DAYS[mondayIndex(iso)]?.id ?? 'MONDAY'
}

export function weekStartOf(iso: string): string {
  return addDays(iso, -mondayIndex(iso))
}

export function shortDay(day: DayOfWeek): string {
  return WEEK_DAYS.find((d) => d.id === day)?.short ?? ''
}

export function longDay(day: DayOfWeek): string {
  return WEEK_DAYS.find((d) => d.id === day)?.long ?? ''
}

/** "qua, 30 set" */
export function formatDay(iso: string): string {
  const date = toUtc(iso)
  return `${shortDay(dayOfWeekOf(iso))}, ${date.getUTCDate()} ${MONTHS[date.getUTCMonth()] ?? ''}`
}

/** "30 set" */
export function formatShortDate(iso: string): string {
  const date = toUtc(iso)
  return `${date.getUTCDate()} ${MONTHS[date.getUTCMonth()] ?? ''}`
}

export function datesBetween(from: string, to: string): string[] {
  const dates: string[] = []
  for (let date = from; date <= to; date = addDays(date, 1)) dates.push(date)
  return dates
}

/** "08:00:00" vira "08:00". */
export function formatTime(time: string | null | undefined): string | null {
  return time ? time.slice(0, 5) : null
}

export function minutesOf(time: string): number {
  const [hours = 0, minutes = 0] = time.split(':').map(Number)
  return hours * 60 + minutes
}

/** "seg, qua e sex" */
export function listJoin(items: string[]): string {
  if (items.length <= 1) return items.join('')
  return `${items.slice(0, -1).join(', ')} e ${items[items.length - 1]}`
}

export function plural(count: number, one: string, many: string): string {
  return `${count} ${count === 1 ? one : many}`
}
