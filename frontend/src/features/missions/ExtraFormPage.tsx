import { useState, type CSSProperties, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router'

import { asApiError } from '../../api/errors'
import type { TaskCategory } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { Field, SelectField } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { CATEGORIES, categoryColor } from '../../lib/categories'
import { safeTimeZone } from '../../lib/datetime'
import { addDays, datesBetween, formatDay, todayIso, weekStartOf } from '../../lib/days'
import { useCreateExtra } from '../planning/planningApi'
import styles from './Form.module.css'

/** Extra avulsa: uma tarefa de um dia só, a partir de amanhã, que vale 1 ou 2 pontos e não afeta a sequência. */
export function ExtraFormPage() {
  const navigate = useNavigate()
  const user = useCurrentUser()
  const [params] = useSearchParams()
  const today = todayIso(safeTimeZone(user.timeZone))
  const options = datesBetween(addDays(today, 1), addDays(weekStartOf(today), 13))
  const requested = params.get('data')
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState<TaskCategory>('HOME')
  const [points, setPoints] = useState(1)
  const [date, setDate] = useState(requested && options.includes(requested) ? requested : (options[0] ?? today))
  const [time, setTime] = useState('')
  const [requiresProof, setRequiresProof] = useState(false)
  const create = useCreateExtra()
  const apiError = create.error ? asApiError(create.error) : null

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    create.mutate(
      { title, category, points, date, time: time || null, durationMinutes: null, requiresProof },
      { onSuccess: () => void navigate('/semana') },
    )
  }

  return (
    <div className={styles.page}>
      <PageTitle title="Nova extra" />
      <h1 className={styles.heading}>Nova extra</h1>
      <p className={styles.note}>Uma tarefa avulsa para um dia a partir de amanhã. Rende 1 moeda e não mexe na sequência.</p>
      <form className={styles.form} onSubmit={submit} noValidate>
        <Field
          label="O que fazer"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={60}
          required
          placeholder="Arrumar a mesa"
          error={apiError?.fieldErrors.title}
        />
        <fieldset className={styles.fieldset}>
          <legend className={styles.legend}>Categoria</legend>
          <div className={styles.chips}>
            {CATEGORIES.map((option) => (
              <label key={option.id} className={styles.chip} style={{ '--cat': categoryColor(option.id) } as CSSProperties}>
                <input type="radio" name="category" checked={category === option.id} onChange={() => setCategory(option.id)} />
                <span className={styles.dot} aria-hidden="true" />
                {option.label}
              </label>
            ))}
          </div>
        </fieldset>
        <fieldset className={styles.fieldset}>
          <legend className={styles.legend}>Pontos</legend>
          <div className={styles.chips}>
            {[1, 2].map((value) => (
              <label key={value} className={styles.chip}>
                <input type="radio" name="points" checked={points === value} onChange={() => setPoints(value)} />
                {value === 1 ? '1 ponto' : '2 pontos'}
              </label>
            ))}
          </div>
        </fieldset>
        <SelectField label="Dia" value={date} onChange={(event) => setDate(event.target.value)}>
          {options.map((option) => (
            <option key={option} value={option}>
              {formatDay(option)}
            </option>
          ))}
        </SelectField>
        <Field label="Horário" type="time" value={time} onChange={(event) => setTime(event.target.value)} hint="Opcional." />
        <label className={styles.check}>
          <input type="checkbox" checked={requiresProof} onChange={(event) => setRequiresProof(event.target.checked)} />
          <span className={styles.checkText}>
            <span className={styles.checkLabel}>Pedir foto como prova</span>
          </span>
        </label>
        {apiError && Object.keys(apiError.fieldErrors).length === 0 && (
          <p className={styles.error} role="alert">
            {apiError.message}
          </p>
        )}
        <div className={styles.actions}>
          <Button type="submit" block disabled={create.isPending}>
            Criar extra
          </Button>
          <ButtonLink to="/semana" variant="secondary" block>
            Cancelar
          </ButtonLink>
        </div>
      </form>
    </div>
  )
}
