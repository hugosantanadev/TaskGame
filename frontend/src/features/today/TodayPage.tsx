import { useState } from 'react'

import { asApiError } from '../../api/errors'
import type { Occurrence, Period, Today } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { CoinIcon, FlameIcon, PlusIcon, StarIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { DAY_PERIODS, describeDay, hourIn, safeTimeZone, timeOfDay } from '../../lib/datetime'
import { formatTime, minutesOf, plural } from '../../lib/days'
import { useNow } from '../../lib/useNow'
import { CompleteSheet } from './CompleteSheet'
import { DayTabs } from './DayTabs'
import { OccurrenceRow } from './OccurrenceRow'
import styles from './TodayPage.module.css'
import { useToday } from './todayApi'

const GREETINGS: Record<Period, string> = {
  MORNING: 'Bom dia',
  AFTERNOON: 'Boa tarde',
  SUNSET: 'Boa tarde',
  NIGHT: 'Boa noite',
}

export function TodayPage() {
  const user = useCurrentUser()
  const now = useNow()
  const zone = safeTimeZone(user.timeZone)
  const today = useToday()
  const [selected, setSelected] = useState<Occurrence | null>(null)
  const day = describeDay(now, zone)
  const period: Period = timeOfDay(hourIn(zone, now))
  const firstName = user.displayName.trim().split(/\s+/)[0]
  const data = today.data
  // Mostra no painel a versão mais nova da tarefa (depois de concluir, a lista é recarregada)
  const current = selected && data ? (data.occurrences.find((o) => o.id === selected.id) ?? selected) : selected

  return (
    <div className={styles.page} data-period={period}>
      <PageTitle title="Hoje" />
      <header className={styles.sky}>
        <p className={styles.greeting}>
          {GREETINGS[period]}, {firstName}.
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
        {data && (
          <ul className={styles.chips} aria-label="Seu placar">
            <li className={styles.streakChip}>
              <FlameIcon /> {plural(data.streak.current, 'dia', 'dias')}
              <span className="visually-hidden"> de sequência</span>
            </li>
            <li className={styles.coinChip}>
              <CoinIcon /> {data.walletBalance}
              <span className="visually-hidden"> moedas</span>
            </li>
            <li className={styles.pointChip}>
              <StarIcon /> {plural(data.progress.points, 'ponto', 'pontos')} hoje
            </li>
          </ul>
        )}
      </header>

      <DayTabs />

      {today.isPending && <p className={styles.loading}>Carregando o dia…</p>}
      {today.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(today.error).message}</p>
          <Button variant="secondary" onClick={() => void today.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}
      {data && <DayContent data={data} period={period} onSelect={setSelected} />}

      <CompleteSheet occurrence={current} isToday onClose={() => setSelected(null)} />
    </div>
  )
}

function DayContent({ data, period, onSelect }: { data: Today; period: Period; onSelect: (o: Occurrence) => void }) {
  const next = data.occurrences.find((o) => o.id === data.nextOccurrenceId) ?? null
  const untimed = data.occurrences.filter((o) => !o.plannedTime)

  return (
    <>
      <ProgressPanel data={data} />

      {next && (
        <button type="button" className={styles.next} onClick={() => onSelect(next)} aria-haspopup="dialog">
          <span className={styles.nextLabel}>Próxima</span>
          <span className={styles.nextTitle}>{next.title}</span>
          <span className={styles.nextTime}>{formatTime(next.plannedTime) ?? 'sem horário'}</span>
        </button>
      )}

      <section className={styles.agenda} aria-labelledby="agenda-title">
        <h2 id="agenda-title" className="visually-hidden">
          Agenda de hoje
        </h2>
        {DAY_PERIODS.map((slot) => {
          const items = inPeriod(data.occurrences, slot.id)
          return (
            <div key={slot.id} className={styles.period} data-current={slot.id === period || undefined}>
              <h3 className={styles.periodHead}>
                <span className={styles.periodStart}>{slot.start}</span>
                <span className={styles.periodName}>
                  {slot.label}
                  {slot.id === period && <span className={styles.now}>agora</span>}
                </span>
              </h3>
              {items.length > 0 && (
                <ul className={styles.list}>
                  {items.map((o) => (
                    <OccurrenceRow key={o.id} occurrence={o} next={o.id === next?.id} onSelect={onSelect} />
                  ))}
                </ul>
              )}
            </div>
          )
        })}
        {untimed.length > 0 && (
          <div className={styles.period}>
            <h3 className={styles.periodHead}>
              <span className={styles.periodStart} />
              <span className={styles.periodName}>Sem horário</span>
            </h3>
            <ul className={styles.list}>
              {untimed.map((o) => (
                <OccurrenceRow key={o.id} occurrence={o} next={o.id === next?.id} onSelect={onSelect} />
              ))}
            </ul>
          </div>
        )}
        {data.occurrences.length === 0 && (
          <div className={styles.empty}>
            <p className={styles.emptyTitle}>Nenhuma tarefa para hoje.</p>
            <p className={styles.emptyText}>
              {data.onboarding
                ? 'É seu primeiro dia: o que você criar agora já pode entrar no plano de hoje.'
                : 'Crie uma missão ou inclua uma das suas no dia de hoje pela aba Semana.'}
            </p>
          </div>
        )}
      </section>

      <div className={styles.actions}>
        <ButtonLink to="/missoes/nova">
          <PlusIcon /> Nova missão
        </ButtonLink>
        <ButtonLink to="/missoes" variant="secondary">
          Minhas missões
        </ButtonLink>
      </div>
    </>
  )
}

function ProgressPanel({ data }: { data: Today }) {
  const { mandatoryPlanned: planned, mandatoryDone: done } = data.progress
  const missing = planned - done
  let message: string
  if (data.streak.todayStatus === 'REST') message = 'Dia de descanso: sem obrigatórias, a sequência fica como está.'
  else if (data.streak.todayStatus === 'FULFILLED') message = `Dia cumprido. Sequência de ${plural(data.streak.current, 'dia', 'dias')}.`
  else message = `${missing === 1 ? 'Falta 1 obrigatória' : `Faltam ${missing} obrigatórias`} para manter a sequência.`

  return (
    <section className={styles.progress} data-status={data.streak.todayStatus} aria-labelledby="progress-title">
      <h2 id="progress-title" className={styles.progressTitle}>
        {message}
      </h2>
      {planned > 0 && (
        <>
          <div
            className={styles.bar}
            role="progressbar"
            aria-label="Obrigatórias concluídas"
            aria-valuemin={0}
            aria-valuemax={planned}
            aria-valuenow={done}
          >
            <span style={{ width: `${(done / planned) * 100}%` }} />
          </div>
          <p className={styles.progressText}>
            {done} de {plural(planned, 'obrigatória', 'obrigatórias')}
            {data.progress.extrasPlanned > 0 &&
              `, ${data.progress.extrasDone} de ${plural(data.progress.extrasPlanned, 'extra', 'extras')}`}
          </p>
        </>
      )}
    </section>
  )
}

/** Agrupa pelo período do horário planejado; na noite, 19h–23h vêm antes da madrugada. */
function inPeriod(occurrences: Occurrence[], period: Period): Occurrence[] {
  const key = (o: Occurrence) => {
    const minutes = minutesOf(o.plannedTime ?? '00:00')
    return minutes < 300 ? minutes + 1440 : minutes
  }
  return occurrences
    .filter((o) => o.plannedTime && timeOfDay(Number(o.plannedTime.slice(0, 2))) === period)
    .sort((a, b) => key(a) - key(b))
}
