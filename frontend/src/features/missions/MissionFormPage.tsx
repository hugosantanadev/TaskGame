import { useState, type CSSProperties, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router'

import { asApiError } from '../../api/errors'
import type { DayOfWeek, Mission, MissionInput, TaskCategory, TaskKind } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { Field } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { CATEGORIES, categoryColor } from '../../lib/categories'
import { safeTimeZone } from '../../lib/datetime'
import { dayOfWeekOf, formatTime, longDay, todayIso, WEEK_DAYS } from '../../lib/days'
import styles from './Form.module.css'
import { fetchSuggestion, useArchiveMission, useMission, useSaveMission } from './missionsApi'

type FormState = {
  name: string
  description: string
  category: TaskCategory
  kind: TaskKind
  points: number
  days: DayOfWeek[]
  time: string
  duration: string
  requiresProof: boolean
  startToday: boolean
}

const EMPTY: FormState = {
  name: '',
  description: '',
  category: 'STUDY',
  kind: 'MANDATORY',
  points: 1,
  days: [],
  time: '',
  duration: '',
  requiresProof: false,
  startToday: true,
}

function fromMission(mission: Mission): FormState {
  return {
    name: mission.name,
    description: mission.description ?? '',
    category: mission.category,
    kind: mission.kind,
    points: mission.kind === 'EXTRA' ? mission.points : 1,
    days: mission.schedule.map((entry) => entry.dayOfWeek),
    time: formatTime(mission.schedule.find((entry) => entry.time)?.time) ?? '',
    duration: mission.durationMinutes ? String(mission.durationMinutes) : '',
    requiresProof: mission.requiresProof,
    startToday: false,
  }
}

export function MissionFormPage() {
  const { id } = useParams()
  const mission = useMission(id)
  if (id && mission.isPending) return <p>Carregando a missão…</p>
  if (id && mission.isError) {
    return (
      <p className={styles.error} role="alert">
        {asApiError(mission.error).message}
      </p>
    )
  }
  return <MissionForm key={id ?? 'nova'} mission={mission.data} />
}

function MissionForm({ mission }: { mission?: Mission }) {
  const navigate = useNavigate()
  const user = useCurrentUser()
  const todayDay = dayOfWeekOf(todayIso(safeTimeZone(user.timeZone)))
  const [form, setForm] = useState<FormState>(mission ? fromMission(mission) : EMPTY)
  const [timesPerWeek, setTimesPerWeek] = useState('3')
  const [daysError, setDaysError] = useState<string | null>(null)
  const save = useSaveMission()
  const archive = useArchiveMission()
  const apiError = save.error ? asApiError(save.error) : null
  const editing = mission !== undefined
  const includesToday = form.days.includes(todayDay)
  const set = (changes: Partial<FormState>) => setForm((previous) => ({ ...previous, ...changes }))

  const toggleDay = (day: DayOfWeek) =>
    set({ days: form.days.includes(day) ? form.days.filter((d) => d !== day) : [...form.days, day] })

  const suggest = async () => {
    try {
      const suggestion = await fetchSuggestion(Number(timesPerWeek))
      set({ days: suggestion.days })
      setDaysError(null)
    } catch (error) {
      setDaysError(asApiError(error).message)
    }
  }

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (form.days.length === 0) {
      setDaysError('Escolha pelo menos um dia.')
      return
    }
    setDaysError(null)
    const input: MissionInput = {
      name: form.name,
      description: form.description.trim() || null,
      category: form.category,
      kind: form.kind,
      points: form.kind === 'EXTRA' ? form.points : null,
      durationMinutes: form.duration ? Number(form.duration) : null,
      requiresProof: form.requiresProof,
      schedule: WEEK_DAYS.filter((day) => form.days.includes(day.id)).map((day) => ({
        dayOfWeek: day.id,
        time: form.time || null,
      })),
      startToday: !editing && form.startToday && includesToday,
    }
    save.mutate({ id: mission?.id, input }, { onSuccess: () => void navigate(editing ? '/missoes' : '/') })
  }

  return (
    <div className={styles.page}>
      <PageTitle title={editing ? 'Editar missão' : 'Nova missão'} />
      <h1 className={styles.heading}>{editing ? 'Editar missão' : 'Nova missão'}</h1>
      {editing && <p className={styles.note}>As mudanças valem a partir de amanhã. O que já está no plano de hoje fica como está.</p>}

      <form className={styles.form} onSubmit={submit} noValidate>
        <Field
          label="Nome"
          value={form.name}
          onChange={(event) => set({ name: event.target.value })}
          maxLength={60}
          required
          placeholder="Estudar Java"
          error={apiError?.fieldErrors.name}
        />

        <fieldset className={styles.fieldset}>
          <legend className={styles.legend}>Categoria</legend>
          <div className={styles.chips}>
            {CATEGORIES.map((category) => (
              <label key={category.id} className={styles.chip} style={{ '--cat': categoryColor(category.id) } as CSSProperties}>
                <input
                  type="radio"
                  name="category"
                  checked={form.category === category.id}
                  onChange={() => set({ category: category.id })}
                />
                <span className={styles.dot} aria-hidden="true" />
                {category.label}
              </label>
            ))}
          </div>
        </fieldset>

        <fieldset className={styles.fieldset}>
          <legend className={styles.legend}>Tipo</legend>
          <div className={styles.chips}>
            <label className={styles.chip}>
              <input type="radio" name="kind" checked={form.kind === 'MANDATORY'} onChange={() => set({ kind: 'MANDATORY' })} />
              Obrigatória
            </label>
            <label className={styles.chip}>
              <input type="radio" name="kind" checked={form.kind === 'EXTRA'} onChange={() => set({ kind: 'EXTRA' })} />
              Extra
            </label>
          </div>
          <p className={styles.hint}>
            {form.kind === 'MANDATORY'
              ? 'Vale 5 pontos e 3 moedas. Conta para a sequência: o dia só é cumprido com todas as obrigatórias feitas.'
              : 'Vale 1 ou 2 pontos e 1 moeda. Não afeta a sequência.'}
          </p>
        </fieldset>

        {form.kind === 'EXTRA' && (
          <fieldset className={styles.fieldset}>
            <legend className={styles.legend}>Pontos</legend>
            <div className={styles.chips}>
              {[1, 2].map((points) => (
                <label key={points} className={styles.chip}>
                  <input type="radio" name="points" checked={form.points === points} onChange={() => set({ points })} />
                  {points === 1 ? '1 ponto' : '2 pontos'}
                </label>
              ))}
            </div>
          </fieldset>
        )}

        <fieldset className={styles.fieldset} aria-describedby={daysError ? 'days-error' : undefined}>
          <legend className={styles.legend}>Dias da semana</legend>
          <div className={styles.days}>
            {WEEK_DAYS.map((day) => (
              <label key={day.id} className={styles.chip} title={day.long}>
                <input type="checkbox" checked={form.days.includes(day.id)} onChange={() => toggleDay(day.id)} aria-label={day.long} />
                <span aria-hidden="true">{day.short}</span>
              </label>
            ))}
          </div>
          <div className={styles.suggest}>
            <label htmlFor="times-per-week" className={styles.hint}>
              Ou escolha quantas vezes por semana:
            </label>
            <select id="times-per-week" value={timesPerWeek} onChange={(event) => setTimesPerWeek(event.target.value)}>
              {[1, 2, 3, 4, 5, 6, 7].map((n) => (
                <option key={n} value={n}>
                  {n}×
                </option>
              ))}
            </select>
            <Button variant="quiet" onClick={() => void suggest()}>
              Sugerir dias
            </Button>
          </div>
          {(daysError ?? apiError?.fieldErrors.schedule) && (
            <p id="days-error" className={styles.fieldError}>
              {daysError ?? apiError?.fieldErrors.schedule}
            </p>
          )}
        </fieldset>

        <Field
          label="Horário"
          type="time"
          value={form.time}
          onChange={(event) => set({ time: event.target.value })}
          hint="Opcional, vale para todos os dias escolhidos. Concluir perto do horário rende 1 moeda a mais."
        />
        <Field
          label="Duração em minutos"
          type="number"
          inputMode="numeric"
          min={1}
          max={720}
          value={form.duration}
          onChange={(event) => set({ duration: event.target.value })}
          hint="Opcional. Aumenta a janela do bônus de horário."
          error={apiError?.fieldErrors.durationMinutes}
        />
        <Field
          label="Descrição"
          value={form.description}
          onChange={(event) => set({ description: event.target.value })}
          maxLength={280}
          hint="Opcional."
        />

        <label className={styles.check}>
          <input type="checkbox" checked={form.requiresProof} onChange={(event) => set({ requiresProof: event.target.checked })} />
          <span className={styles.checkText}>
            <span className={styles.checkLabel}>Pedir foto como prova</span>
            <span className={styles.hint}>A tarefa só pode ser concluída com uma foto.</span>
          </span>
        </label>

        {!editing && includesToday && (
          <label className={styles.check}>
            <input type="checkbox" checked={form.startToday} onChange={(event) => set({ startToday: event.target.checked })} />
            <span className={styles.checkText}>
              <span className={styles.checkLabel}>Incluir hoje</span>
              <span className={styles.hint}>
                Hoje é {longDay(todayDay)}: a missão já entra no plano de hoje. Desmarque para começar amanhã.
              </span>
            </span>
          </label>
        )}

        {apiError && Object.keys(apiError.fieldErrors).length === 0 && (
          <p className={styles.error} role="alert">
            {apiError.message}
          </p>
        )}

        <div className={styles.actions}>
          <Button type="submit" block disabled={save.isPending}>
            {editing ? 'Salvar alterações' : 'Criar missão'}
          </Button>
          <ButtonLink to={editing ? '/missoes' : '/'} variant="secondary" block>
            Cancelar
          </ButtonLink>
          {editing && (
            <Button
              variant="quiet"
              tone="danger"
              disabled={archive.isPending}
              onClick={() => {
                if (window.confirm('Arquivar esta missão? Ela sai do plano a partir de amanhã e o histórico continua.')) {
                  archive.mutate(mission.id, { onSuccess: () => void navigate('/missoes') })
                }
              }}
            >
              Arquivar missão
            </Button>
          )}
        </div>
      </form>
    </div>
  )
}
