import { useEffect, useState } from 'react'

import type { UpcomingReminder } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { BellIcon, CrossIcon, MoonIcon } from '../../components/gameIcons'
import { safeTimeZone } from '../../lib/datetime'
import styles from './ReminderNotifier.module.css'
import { useUpcomingReminders } from './remindersApi'

/** Avisos já mostrados nesta aba: recalcular a lista não repete o mesmo lembrete. */
const shown = new Set<string>()

function keyOf(reminder: UpcomingReminder): string {
  return `${reminder.kind}|${reminder.eventAt}|${reminder.occurrenceId ?? ''}`
}

function timeIn(zone: string, iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: zone }).format(new Date(iso))
}

function reminderText(reminder: UpcomingReminder, zone: string): { title: string; body: string } {
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

/** Notificação do sistema, quando a pessoa permitiu; pelo service worker, que funciona também no celular. */
async function showSystemNotification(reminder: UpcomingReminder, zone: string): Promise<boolean> {
  if (!('Notification' in window) || Notification.permission !== 'granted') return false
  const { title, body } = reminderText(reminder, zone)
  const options = { body, tag: keyOf(reminder), icon: '/icons/pwa-192.png' }
  try {
    const registration = await navigator.serviceWorker?.getRegistration()
    if (registration) {
      await registration.showNotification(title, options)
    } else {
      new Notification(title, options)
    }
    return true
  } catch {
    return false
  }
}

/**
 * Lembretes enquanto o app está aberto (RF23, MVP): agenda os avisos das próximas horas. Com a tela à vista,
 * o aviso aparece aqui dentro; em segundo plano, vira notificação do sistema (se permitida). Com o app
 * fechado, só quando o Web Push chegar.
 */
export function ReminderNotifier() {
  const zone = safeTimeZone(useCurrentUser().timeZone)
  const upcoming = useUpcomingReminders()
  const [notice, setNotice] = useState<UpcomingReminder | null>(null)

  useEffect(() => {
    const now = Date.now()
    const timers = (upcoming.data ?? [])
      .filter((reminder) => !shown.has(keyOf(reminder)))
      .map((reminder) => ({ reminder, delay: Date.parse(reminder.notifyAt) - now }))
      .filter(({ delay }) => delay >= 0)
      .map(({ reminder, delay }) =>
        window.setTimeout(() => {
          shown.add(keyOf(reminder))
          if (document.visibilityState === 'visible') {
            setNotice(reminder)
            return
          }
          void showSystemNotification(reminder, zone).then((sent) => {
            if (!sent) setNotice(reminder)
          })
        }, delay),
      )
    return () => timers.forEach((timer) => window.clearTimeout(timer))
  }, [upcoming.data, zone])

  useEffect(() => {
    if (!notice) return
    const timer = window.setTimeout(() => setNotice(null), 20_000)
    return () => window.clearTimeout(timer)
  }, [notice])

  const text = notice ? reminderText(notice, zone) : null
  return (
    <div className={styles.region} role="status" aria-live="polite">
      {notice && text && (
        <div className={styles.notice} data-kind={notice.kind}>
          <span className={styles.icon} aria-hidden="true">
            {notice.kind === 'BEDTIME' ? <MoonIcon /> : <BellIcon />}
          </span>
          <div className={styles.text}>
            <p className={styles.title}>{text.title}</p>
            <p className={styles.body}>{text.body}</p>
          </div>
          <button type="button" className={styles.close} onClick={() => setNotice(null)} aria-label="Fechar lembrete">
            <CrossIcon />
          </button>
        </div>
      )}
    </div>
  )
}
