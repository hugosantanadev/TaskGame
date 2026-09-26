/** Datas sempre no fuso do usuário, nunca no do aparelho (o mesmo critério do backend). */

export type TimeOfDay = 'MORNING' | 'AFTERNOON' | 'SUNSET' | 'NIGHT'

export const DAY_PERIODS: ReadonlyArray<{ id: TimeOfDay; label: string; start: string }> = [
  { id: 'MORNING', label: 'Manhã', start: '05h' },
  { id: 'AFTERNOON', label: 'Tarde', start: '12h' },
  { id: 'SUNSET', label: 'Pôr do sol', start: '17h' },
  { id: 'NIGHT', label: 'Noite', start: '19h' },
]

export function detectTimeZone(): string {
  return Intl.DateTimeFormat().resolvedOptions().timeZone || 'America/Sao_Paulo'
}

/** Fuso válido para o Intl; se o salvo não for reconhecido pelo navegador, usa o do aparelho. */
export function safeTimeZone(timeZone: string): string {
  try {
    new Intl.DateTimeFormat('pt-BR', { timeZone })
    return timeZone
  } catch {
    return detectTimeZone()
  }
}

export function hourIn(timeZone: string, date: Date): number {
  return Number(new Intl.DateTimeFormat('en-US', { timeZone, hour: 'numeric', hourCycle: 'h23' }).format(date))
}

/** Manhã 05:00–11:59, tarde 12:00–16:59, pôr do sol 17:00–18:59, noite 19:00–04:59. */
export function timeOfDay(hour: number): TimeOfDay {
  if (hour >= 5 && hour < 12) return 'MORNING'
  if (hour >= 12 && hour < 17) return 'AFTERNOON'
  if (hour >= 17 && hour < 19) return 'SUNSET'
  return 'NIGHT'
}

export function describeDay(date: Date, timeZone: string) {
  const parts = new Intl.DateTimeFormat('pt-BR', {
    timeZone,
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).formatToParts(date)
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((p) => p.type === type)?.value ?? ''
  return { weekday: part('weekday'), day: part('day'), month: part('month'), year: part('year') }
}

export function formatLongDate(isoInstant: string, timeZone: string): string {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long', timeZone }).format(new Date(isoInstant))
}

/** Todos os fusos IANA que o navegador conhece, garantindo que o atual esteja na lista. */
export function timeZoneOptions(current: string): string[] {
  const zones = typeof Intl.supportedValuesOf === 'function' ? Intl.supportedValuesOf('timeZone') : []
  return zones.includes(current) ? zones : [current, ...zones]
}
