import { useMemo, useState, type FormEvent } from 'react'

import type { ReminderSettings, User } from '../../api/types'
import { asApiError } from '../../api/errors'
import { useAuth, useCurrentUser } from '../../auth/context'
import { Link } from 'react-router'

import { Button } from '../../components/Button'
import { ShirtIcon, SofaIcon, TrophyIcon } from '../../components/gameIcons'
import { Field, FormAlert, SelectField } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { formatLongDate, safeTimeZone, timeZoneOptions } from '../../lib/datetime'
import { formatTime } from '../../lib/days'
import { useReminderSettings, useUpdateReminderSettings } from '../reminders/remindersApi'
import styles from './ProfilePage.module.css'
import { useProfile, useUpdateProfile, type ProfileChanges } from './profileApi'

export function ProfilePage() {
  const { logout } = useAuth()
  const { data: profile } = useProfile(useCurrentUser())

  return (
    <div className={styles.page}>
      <PageTitle title="Perfil" />
      <h1 className={styles.title}>Perfil</h1>

      <nav className={styles.collection} aria-label="Sua coleção">
        <Link to="/perfil/conquistas" className={styles.collectionLink}>
          <TrophyIcon />
          Conquistas
        </Link>
        <Link to="/perfil/quarto" className={styles.collectionLink}>
          <SofaIcon />
          Quarto
        </Link>
        <Link to="/perfil/personagem" className={styles.collectionLink}>
          <ShirtIcon />
          Personagem
        </Link>
      </nav>

      <section className={styles.section} aria-labelledby="profile-data">
        <h2 id="profile-data" className={styles.sectionTitle}>
          Seus dados
        </h2>
        <ProfileForm profile={profile} />
      </section>

      <section className={styles.section} aria-labelledby="profile-reminders">
        <h2 id="profile-reminders" className={styles.sectionTitle}>
          Lembretes
        </h2>
        <RemindersSection />
      </section>

      <section className={styles.section} aria-labelledby="profile-account">
        <h2 id="profile-account" className={styles.sectionTitle}>
          Conta
        </h2>
        <dl className={styles.facts}>
          <div className={styles.fact}>
            <dt>E-mail</dt>
            <dd>{profile.email}</dd>
          </div>
          <div className={styles.fact}>
            <dt>Desde</dt>
            <dd>{formatLongDate(profile.createdAt, safeTimeZone(profile.timeZone))}</dd>
          </div>
        </dl>
        <Button variant="secondary" tone="danger" className={styles.logout} onClick={() => void logout()}>
          Sair da conta
        </Button>
      </section>
    </div>
  )
}

function ProfileForm({ profile }: { profile: User }) {
  const update = useUpdateProfile()
  const [displayName, setDisplayName] = useState(profile.displayName)
  const [timeZone, setTimeZone] = useState(profile.timeZone)
  const [rankingVisible, setRankingVisible] = useState(profile.rankingVisible)
  const zones = useMemo(() => timeZoneOptions(profile.timeZone), [profile.timeZone])

  const changes = changesBetween(profile, { displayName: displayName.trim(), timeZone, rankingVisible })
  const dirty = Object.keys(changes).length > 0
  const error = update.error ? asApiError(update.error) : null

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (dirty) update.mutate(changes)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      {error && <FormAlert>{error.message}</FormAlert>}
      <Field
        label="Nome"
        name="displayName"
        autoComplete="nickname"
        required
        minLength={2}
        maxLength={40}
        value={displayName}
        onChange={(event) => setDisplayName(event.target.value)}
        error={error?.fieldErrors.displayName}
      />
      <SelectField
        label="Fuso horário"
        name="timeZone"
        hint="Define quando cada dia começa e termina para as suas missões."
        value={timeZone}
        onChange={(event) => setTimeZone(event.target.value)}
        error={error?.fieldErrors.timeZone}
      >
        {zones.map((zone) => (
          <option key={zone} value={zone}>
            {zone.replaceAll('_', ' ')}
          </option>
        ))}
      </SelectField>
      <div className={styles.toggle}>
        <input
          id="ranking-visible"
          type="checkbox"
          role="switch"
          className={styles.switch}
          checked={rankingVisible}
          onChange={(event) => setRankingVisible(event.target.checked)}
          aria-describedby="ranking-visible-hint"
        />
        <label htmlFor="ranking-visible" className={styles.toggleLabel}>
          Aparecer no ranking
        </label>
        <p id="ranking-visible-hint" className={styles.toggleHint}>
          Desligado, seu nome não aparece no ranking para outras pessoas.
        </p>
      </div>
      <div className={styles.actions}>
        <Button type="submit" disabled={!dirty || update.isPending}>
          {update.isPending ? 'Salvando…' : 'Salvar alterações'}
        </Button>
        <span className={styles.saved} role="status">
          {update.isSuccess && !dirty ? 'Alterações salvas.' : ''}
        </span>
      </div>
    </form>
  )
}

function changesBetween(profile: User, draft: Required<ProfileChanges>): ProfileChanges {
  const changes: ProfileChanges = {}
  if (draft.displayName !== profile.displayName) changes.displayName = draft.displayName
  if (draft.timeZone !== profile.timeZone) changes.timeZone = draft.timeZone
  if (draft.rankingVisible !== profile.rankingVisible) changes.rankingVisible = draft.rankingVisible
  return changes
}

// ------------------------------------------------------------------ lembretes

const LEAD_OPTIONS = [0, 5, 10, 15, 30, 60, 120]

function leadLabel(minutes: number): string {
  if (minutes === 0) return 'Na hora'
  return minutes < 60 ? `${minutes} minutos antes` : `${minutes / 60} ${minutes === 60 ? 'hora' : 'horas'} antes`
}

function RemindersSection() {
  const settings = useReminderSettings()
  if (settings.isPending) return <p className={styles.toggleHint}>Carregando…</p>
  if (settings.isError) return <FormAlert>{asApiError(settings.error).message}</FormAlert>
  return (
    <>
      <ReminderForm saved={settings.data} />
      <NotificationPermission />
    </>
  )
}

function ReminderForm({ saved }: { saved: ReminderSettings }) {
  const update = useUpdateReminderSettings()
  const [tasksEnabled, setTasksEnabled] = useState(saved.tasksEnabled)
  const [leadMinutes, setLeadMinutes] = useState(saved.leadMinutes)
  const [bedtime, setBedtime] = useState(formatTime(saved.bedtime) ?? '')
  const [wakeTime, setWakeTime] = useState(formatTime(saved.wakeTime) ?? '')
  const draft: ReminderSettings = { tasksEnabled, leadMinutes, bedtime: bedtime || null, wakeTime: wakeTime || null }
  const dirty =
    draft.tasksEnabled !== saved.tasksEnabled ||
    draft.leadMinutes !== saved.leadMinutes ||
    draft.bedtime !== (formatTime(saved.bedtime) ?? null) ||
    draft.wakeTime !== (formatTime(saved.wakeTime) ?? null)
  const error = update.error ? asApiError(update.error) : null

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (dirty) update.mutate(draft)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      {error && <FormAlert>{error.message}</FormAlert>}
      <div className={styles.toggle}>
        <input
          id="reminders-tasks"
          type="checkbox"
          role="switch"
          className={styles.switch}
          checked={tasksEnabled}
          onChange={(event) => setTasksEnabled(event.target.checked)}
          aria-describedby="reminders-tasks-hint"
        />
        <label htmlFor="reminders-tasks" className={styles.toggleLabel}>
          Avisar das tarefas
        </label>
        <p id="reminders-tasks-hint" className={styles.toggleHint}>
          Só das que têm horário e ainda estão pendentes.
        </p>
      </div>
      <SelectField
        label="Antecedência"
        name="leadMinutes"
        hint="Vale para as tarefas e para a hora de dormir."
        value={leadMinutes}
        onChange={(event) => setLeadMinutes(Number(event.target.value))}
        error={error?.fieldErrors.leadMinutes}
      >
        {LEAD_OPTIONS.map((minutes) => (
          <option key={minutes} value={minutes}>
            {leadLabel(minutes)}
          </option>
        ))}
      </SelectField>
      <Field
        label="Hora de dormir"
        name="bedtime"
        type="time"
        hint="Deixe em branco para não receber esse lembrete."
        value={bedtime}
        onChange={(event) => setBedtime(event.target.value)}
        error={error?.fieldErrors.bedtime}
      />
      <Field
        label="Hora de acordar"
        name="wakeTime"
        type="time"
        hint="Deixe em branco para não receber esse lembrete."
        value={wakeTime}
        onChange={(event) => setWakeTime(event.target.value)}
        error={error?.fieldErrors.wakeTime}
      />
      <div className={styles.actions}>
        <Button type="submit" disabled={!dirty || update.isPending}>
          {update.isPending ? 'Salvando…' : 'Salvar lembretes'}
        </Button>
        <span className={styles.saved} role="status">
          {update.isSuccess && !dirty ? 'Lembretes salvos.' : ''}
        </span>
      </div>
    </form>
  )
}

type Permission = NotificationPermission | 'unsupported'

function currentPermission(): Permission {
  return 'Notification' in window ? Notification.permission : 'unsupported'
}

/** Os lembretes funcionam com o app aberto; com permissão, também viram aviso do sistema em segundo plano. */
function NotificationPermission() {
  const [permission, setPermission] = useState<Permission>(currentPermission)

  async function ask() {
    setPermission(await Notification.requestPermission())
  }

  return (
    <div className={styles.permission}>
      <p className={styles.toggleHint}>
        Por enquanto os lembretes funcionam com o app aberto, mesmo em outra aba. Com ele fechado, só numa próxima versão.
      </p>
      {permission === 'default' && (
        <Button variant="secondary" onClick={() => void ask()}>
          Permitir avisos neste aparelho
        </Button>
      )}
      {permission === 'granted' && <p className={styles.saved}>Avisos do sistema permitidos neste aparelho.</p>}
      {permission === 'denied' && (
        <p className={styles.toggleHint}>O navegador bloqueou os avisos. Libere nas configurações do site para recebê-los.</p>
      )}
      {permission === 'unsupported' && (
        <p className={styles.toggleHint}>Este navegador não mostra avisos do sistema; os lembretes aparecem dentro do app.</p>
      )}
    </div>
  )
}
