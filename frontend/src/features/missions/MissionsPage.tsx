import { useState, type CSSProperties } from 'react'
import { Link } from 'react-router'

import { asApiError } from '../../api/errors'
import type { Mission } from '../../api/types'
import { ButtonLink } from '../../components/Button'
import { PlusIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { ToggleGroup } from '../../components/ToggleGroup'
import { categoryColor, categoryLabel } from '../../lib/categories'
import { plural } from '../../lib/days'
import { scheduleSummary, useMissions } from './missionsApi'
import styles from './MissionsPage.module.css'

const VIEWS = [
  { value: 'active', label: 'Ativas' },
  { value: 'archived', label: 'Arquivadas' },
] as const

export function MissionsPage() {
  const [view, setView] = useState<'active' | 'archived'>('active')
  const missions = useMissions(view === 'archived')

  return (
    <div className={styles.page}>
      <PageTitle title="Missões" />
      <div className={styles.top}>
        <h1 className={styles.heading}>Missões</h1>
        <ButtonLink to="/missoes/nova">
          <PlusIcon /> Nova missão
        </ButtonLink>
      </div>
      <p className={styles.intro}>O que você quer fazer e em quais dias. O plano de cada semana é montado a partir daqui.</p>
      <ToggleGroup label="Quais missões" value={view} options={VIEWS} onChange={setView} />

      {missions.isPending && <p className={styles.note}>Carregando…</p>}
      {missions.isError && (
        <p className={styles.error} role="alert">
          {asApiError(missions.error).message}
        </p>
      )}
      {missions.data && missions.data.length === 0 && (
        <p className={styles.note}>
          {view === 'archived'
            ? 'Nenhuma missão arquivada.'
            : 'Nenhuma missão ainda. Comece por algo como "Estudar Java, 5 vezes por semana, às 8h".'}
        </p>
      )}
      {missions.data && missions.data.length > 0 && (
        <ul className={styles.list}>
          {missions.data.map((mission) => (
            <li key={mission.id} style={{ '--cat': categoryColor(mission.category) } as CSSProperties}>
              {mission.archived ? (
                <div className={styles.card}>
                  <MissionText mission={mission} />
                </div>
              ) : (
                <Link to={`/missoes/${mission.id}`} className={styles.card}>
                  <MissionText mission={mission} />
                </Link>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function MissionText({ mission }: { mission: Mission }) {
  return (
    <>
      <span className={styles.name}>{mission.name}</span>
      <span className={styles.schedule}>{scheduleSummary(mission.schedule)}</span>
      <span className={styles.meta}>
        {categoryLabel(mission.category)}, {mission.kind === 'MANDATORY' ? 'obrigatória' : 'extra'},{' '}
        {plural(mission.points, 'ponto', 'pontos')}
        {mission.requiresProof ? ', pede foto' : ''}
      </span>
    </>
  )
}
