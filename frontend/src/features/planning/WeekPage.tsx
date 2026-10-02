import { useState } from 'react'

import { asApiError } from '../../api/errors'
import type { DayPlan, Occurrence, Week } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { Field, SelectField } from '../../components/Field'
import { PlusIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { Sheet } from '../../components/Sheet'
import { ToggleGroup } from '../../components/ToggleGroup'
import { safeTimeZone } from '../../lib/datetime'
import { addDays, datesBetween, formatDay, formatShortDate, formatTime, longDay, todayIso, weekStartOf } from '../../lib/days'
import { useMissions } from '../missions/missionsApi'
import { CompleteSheet } from '../today/CompleteSheet'
import { DayTabs } from '../today/DayTabs'
import { OccurrenceRow } from '../today/OccurrenceRow'
import { useAddOccurrence, useMoveOccurrence, useRemoveOccurrence, useWeek, type WhichWeek } from './planningApi'
import styles from './WeekPage.module.css'

type AddTarget = { date: string; weekStart: string }

const WEEKS = [
  { value: 'current', label: 'Esta semana' },
  { value: 'next', label: 'Próxima' },
] as const

export function WeekPage() {
  const user = useCurrentUser()
  const today = todayIso(safeTimeZone(user.timeZone))
  const [which, setWhich] = useState<WhichWeek>('current')
  const week = useWeek(which)
  const [adding, setAdding] = useState<AddTarget | null>(null)
  const [selected, setSelected] = useState<Occurrence | null>(null)
  const current = selected && week.data ? (findOccurrence(week.data, selected.id) ?? selected) : selected
  // Hoje e o passado não mudam: tocar nelas abre as ações de conclusão; o resto abre "mover ou remover"
  const planning = current?.removable ?? false

  return (
    <div className={styles.page}>
      <PageTitle title="Semana" />
      <h1 className={styles.heading}>Plano da semana</h1>
      <DayTabs />
      <div className={styles.toolbar}>
        <ToggleGroup label="Qual semana" value={which} options={WEEKS} onChange={setWhich} />
        {week.data && (
          <p className={styles.range}>
            {formatShortDate(week.data.weekStart)} a {formatShortDate(week.data.weekEnd)}
          </p>
        )}
      </div>

      {week.isPending && <p className={styles.note}>Carregando a semana…</p>}
      {week.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(week.error).message}</p>
          <Button variant="secondary" onClick={() => void week.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}
      {week.data?.days.map((day) => (
        <DaySection
          key={day.date}
          day={day}
          onAdd={() => setAdding({ date: day.date, weekStart: week.data.weekStart })}
          onSelect={setSelected}
        />
      ))}

      <div className={styles.actions}>
        <ButtonLink to="/missoes/nova">
          <PlusIcon /> Nova missão
        </ButtonLink>
        <ButtonLink to="/missoes" variant="secondary">
          Minhas missões
        </ButtonLink>
      </div>

      <Sheet open={adding !== null} title={adding ? `Incluir em ${formatDay(adding.date)}` : ''} onClose={() => setAdding(null)}>
        {adding && <AddForm key={adding.date} target={adding} onDone={() => setAdding(null)} />}
      </Sheet>
      <Sheet open={planning && current !== null} title={current?.title ?? ''} onClose={() => setSelected(null)}>
        {planning && current && <PlanForm key={current.id} occurrence={current} today={today} onDone={() => setSelected(null)} />}
      </Sheet>
      <CompleteSheet
        occurrence={planning ? null : current}
        isToday={current?.date === today}
        onClose={() => setSelected(null)}
      />
    </div>
  )
}

function findOccurrence(week: Week, id: string): Occurrence | undefined {
  return week.days.flatMap((day) => day.occurrences).find((occurrence) => occurrence.id === id)
}

function DaySection({ day, onAdd, onSelect }: { day: DayPlan; onAdd: () => void; onSelect: (o: Occurrence) => void }) {
  const titleId = `dia-${day.date}`
  return (
    <section className={styles.day} data-today={day.today || undefined} data-past={day.past || undefined} aria-labelledby={titleId}>
      <div className={styles.dayHead}>
        <h2 id={titleId} className={styles.dayTitle}>
          <span>{longDay(day.dayOfWeek)}</span>
          <span className={styles.dayDate}>{formatShortDate(day.date)}</span>
          {day.today && <span className={styles.todayTag}>hoje</span>}
        </h2>
        {(day.canAddMandatory || day.canAddExtra) && (
          <div className={styles.dayActions}>
            {day.canAddMandatory && (
              <Button variant="quiet" onClick={onAdd} aria-label={`Incluir missão em ${formatDay(day.date)}`}>
                Incluir missão
              </Button>
            )}
            {day.canAddExtra && (
              <ButtonLink variant="quiet" to={`/extras/nova?data=${day.date}`} aria-label={`Criar extra em ${formatDay(day.date)}`}>
                Extra
              </ButtonLink>
            )}
          </div>
        )}
      </div>
      {day.occurrences.length > 0 ? (
        <ul className={styles.list}>
          {day.occurrences.map((occurrence) => (
            <OccurrenceRow key={occurrence.id} occurrence={occurrence} onSelect={onSelect} />
          ))}
        </ul>
      ) : (
        <p className={styles.free}>{day.past ? 'Nada planejado.' : 'Livre.'}</p>
      )}
    </section>
  )
}

function AddForm({ target, onDone }: { target: AddTarget; onDone: () => void }) {
  const missions = useMissions(false)
  const add = useAddOccurrence()
  const [taskId, setTaskId] = useState('')
  const [time, setTime] = useState('')

  if (missions.isPending) return <p className={styles.note}>Carregando suas missões…</p>
  if (!missions.data || missions.data.length === 0) {
    return (
      <div className={styles.form}>
        <p className={styles.note}>Você ainda não tem missões para incluir.</p>
        <ButtonLink to="/missoes/nova" block>
          Criar missão
        </ButtonLink>
      </div>
    )
  }
  const chosen = taskId || missions.data[0].id
  return (
    <form
      className={styles.form}
      onSubmit={(event) => {
        event.preventDefault()
        add.mutate({ weekStart: target.weekStart, taskId: chosen, date: target.date, time: time || null }, { onSuccess: onDone })
      }}
    >
      <SelectField label="Missão" value={chosen} onChange={(event) => setTaskId(event.target.value)}>
        {missions.data.map((mission) => (
          <option key={mission.id} value={mission.id}>
            {mission.name}
            {mission.kind === 'EXTRA' ? ' (extra)' : ''}
          </option>
        ))}
      </SelectField>
      <Field
        label="Horário"
        type="time"
        value={time}
        onChange={(event) => setTime(event.target.value)}
        hint="Opcional. Vazio usa o horário da recorrência desse dia, se houver."
      />
      {add.error && (
        <p className={styles.error} role="alert">
          {asApiError(add.error).message}
        </p>
      )}
      <Button type="submit" block disabled={add.isPending}>
        Incluir no dia
      </Button>
    </form>
  )
}

function PlanForm({ occurrence, today, onDone }: { occurrence: Occurrence; today: string; onDone: () => void }) {
  const move = useMoveOccurrence()
  const remove = useRemoveOccurrence()
  const [date, setDate] = useState(occurrence.date)
  const [time, setTime] = useState(formatTime(occurrence.plannedTime) ?? '')
  const first = occurrence.kind === 'EXTRA' ? addDays(today, 1) : today
  const options = datesBetween(first, addDays(weekStartOf(today), 13))
  if (!options.includes(occurrence.date)) options.unshift(occurrence.date)
  const error = move.error ?? remove.error
  const busy = move.isPending || remove.isPending

  return (
    <div className={styles.form}>
      <p className={styles.note}>
        {formatDay(occurrence.date)}, {formatTime(occurrence.plannedTime) ?? 'sem horário'}
      </p>
      <form
        className={styles.form}
        onSubmit={(event) => {
          event.preventDefault()
          move.mutate({ id: occurrence.id, date, time: time || null }, { onSuccess: onDone })
        }}
      >
        <SelectField label="Dia" value={date} onChange={(event) => setDate(event.target.value)}>
          {options.map((option) => (
            <option key={option} value={option}>
              {formatDay(option)}
            </option>
          ))}
        </SelectField>
        <Field label="Horário" type="time" value={time} onChange={(event) => setTime(event.target.value)} hint="Vazio deixa sem horário." />
        <Button type="submit" block disabled={busy}>
          Salvar mudança
        </Button>
      </form>
      <Button variant="secondary" tone="danger" block disabled={busy} onClick={() => remove.mutate(occurrence.id, { onSuccess: onDone })}>
        Remover do plano
      </Button>
      {occurrence.date === today && <p className={styles.note}>Para concluir, use a tela Hoje.</p>}
      {error && (
        <p className={styles.error} role="alert">
          {asApiError(error).message}
        </p>
      )}
    </div>
  )
}
