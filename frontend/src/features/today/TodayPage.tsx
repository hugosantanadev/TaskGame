import { useCurrentUser } from '../../auth/context'
import { PageTitle } from '../../components/PageTitle'
import { DAY_PERIODS, describeDay, hourIn, safeTimeZone, timeOfDay, type TimeOfDay } from '../../lib/datetime'
import { useNow } from '../../lib/useNow'
import styles from './TodayPage.module.css'

const GREETINGS: Record<TimeOfDay, string> = {
  MORNING: 'Bom dia',
  AFTERNOON: 'Boa tarde',
  SUNSET: 'Boa tarde',
  NIGHT: 'Boa noite',
}

export function TodayPage() {
  const user = useCurrentUser()
  const now = useNow()
  const zone = safeTimeZone(user.timeZone)
  const day = describeDay(now, zone)
  const current = timeOfDay(hourIn(zone, now))
  const firstName = user.displayName.trim().split(/\s+/)[0]

  return (
    <div className={styles.page}>
      <PageTitle title="Hoje" />
      <header className={styles.header}>
        <p className={styles.greeting}>
          {GREETINGS[current]}, {firstName}.
        </p>
        <h1 className={styles.date}>
          <span className="visually-hidden">
            Hoje, {day.weekday}, {day.day} de {day.month} de {day.year}
          </span>
          <span className={styles.day} aria-hidden="true">
            {day.day}
          </span>
          <span className={styles.dateText} aria-hidden="true">
            <span className={styles.weekday}>{day.weekday}</span>
            <span className={styles.month}>
              {day.month} de {day.year}
            </span>
          </span>
        </h1>
      </header>

      <section className={styles.agenda} aria-labelledby="agenda-title">
        <h2 id="agenda-title" className="visually-hidden">
          Agenda de hoje
        </h2>
        <ol className={styles.periods}>
          {DAY_PERIODS.map((period) => (
            <li
              key={period.id}
              className={period.id === current ? `${styles.period} ${styles.current}` : styles.period}
              aria-current={period.id === current ? 'time' : undefined}
            >
              <span className={styles.periodStart}>{period.start}</span>
              <span className={styles.periodName}>
                {period.label}
                {period.id === current && <span className={styles.now}>agora</span>}
              </span>
            </li>
          ))}
        </ol>
        <div className={styles.empty}>
          <p className={styles.emptyTitle}>Nenhuma missão planejada para hoje.</p>
          <p className={styles.emptyText}>Criar missões e montar o plano da semana chega na próxima fase do projeto.</p>
        </div>
      </section>
    </div>
  )
}
