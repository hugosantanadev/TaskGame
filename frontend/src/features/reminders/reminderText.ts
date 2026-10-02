import type { UpcomingReminder } from '../../api/types'

function timeIn(zone: string, iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: zone }).format(new Date(iso))
}

/** Título e texto do aviso; horários no fuso do usuário. */
export function reminderText(reminder: UpcomingReminder, zone: string): { title: string; body: string } {
  const startsNow = reminder.notifyAt === reminder.eventAt
  switch (reminder.kind) {
    case 'TASK':
      return {
        title: startsNow ? `Agora: ${reminder.title ?? 'sua tarefa'}` : `Daqui a pouco: ${reminder.title ?? 'sua tarefa'}`,
        body: `Começa às ${timeIn(zone, reminder.eventAt)}.`,
      }
    case 'BEDTIME':
      return {
        title: 'Hora de se preparar para dormir',
        body: `Dormir às ${timeIn(zone, reminder.eventAt)} deixa o dia de amanhã mais fácil.`,
      }
    case 'WAKE_UP':
      return { title: 'Bom dia! Hora de acordar', body: 'As tarefas de hoje estão na tela Hoje.' }
  }
}
