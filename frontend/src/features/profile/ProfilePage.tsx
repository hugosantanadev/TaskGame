import { useMemo, useState, type FormEvent } from 'react'

import type { User } from '../../api/types'
import { asApiError } from '../../api/errors'
import { useAuth, useCurrentUser } from '../../auth/context'
import { Link } from 'react-router'

import { Button } from '../../components/Button'
import { ShirtIcon, SofaIcon, TrophyIcon } from '../../components/gameIcons'
import { Field, FormAlert, SelectField } from '../../components/Field'
import { PageTitle } from '../../components/PageTitle'
import { formatLongDate, safeTimeZone, timeZoneOptions } from '../../lib/datetime'
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
