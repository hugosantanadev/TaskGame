import { useState, type CSSProperties, type ReactNode } from 'react'

import { asApiError } from '../../api/errors'
import type { StatsGranularity, StatsOverview, SummaryDayStatus, WeekSummary } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import {
  CheckIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  CircleIcon,
  CrossIcon,
  MinusIcon,
  TrophyIcon,
} from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { ToggleGroup } from '../../components/ToggleGroup'
import { categoryColor, categoryLabel } from '../../lib/categories'
import { safeTimeZone } from '../../lib/datetime'
import { addDays, formatShortDate, longDay, plural, shortDay, todayIso, weekStartOf } from '../../lib/days'
import { PlannedDoneChart } from './PlannedDoneChart'
import styles from './StatsPage.module.css'
import { useStatsHistory, useStatsOverview, useWeekSummary } from './statsApi'

export function StatsPage() {
  const user = useCurrentUser()
  const zone = safeTimeZone(user.timeZone)
  const currentWeek = weekStartOf(todayIso(zone))
  const firstWeek = weekStartOf(todayIso(zone, new Date(user.createdAt)))

  return (
    <div className={styles.page}>
      <PageTitle title="Estatísticas" />
      <h1 className={styles.heading}>Estatísticas</h1>
      <WeekSummarySection currentWeek={currentWeek} firstWeek={firstWeek} />
      <HistorySection />
      <OverviewSection />
    </div>
  )
}

// ------------------------------------------------------------------ resumo semanal

const DAY_STATUS: Record<SummaryDayStatus, { label: string; icon: ReactNode }> = {
  FULFILLED: { label: 'cumprido', icon: <CheckIcon /> },
  FAILED: { label: 'falhou', icon: <CrossIcon /> },
  REST: { label: 'descanso', icon: <MinusIcon /> },
  PENDING: { label: 'em andamento', icon: <CircleIcon /> },
  UPCOMING: { label: 'ainda não chegou', icon: null },
}

function weekTitle(weekStart: string, currentWeek: string): string {
  if (weekStart === currentWeek) return 'Esta semana'
  if (weekStart === addDays(currentWeek, -7)) return 'Semana passada'
  return `Semana de ${formatShortDate(weekStart)}`
}

function WeekSummarySection({ currentWeek, firstWeek }: { currentWeek: string; firstWeek: string }) {
  const [weekStart, setWeekStart] = useState(currentWeek)
  const summary = useWeekSummary(weekStart)
  const isCurrent = weekStart === currentWeek

  return (
    <section className={styles.card} aria-labelledby="week-summary-title">
      <div className={styles.cardHeader}>
        <div>
          <h2 id="week-summary-title" className={styles.sectionTitle}>
            {weekTitle(weekStart, currentWeek)}
          </h2>
          <p className={styles.range}>
            {formatShortDate(weekStart)} a {formatShortDate(addDays(weekStart, 6))}
            {summary.data && !summary.isPlaceholderData && (summary.data.finished ? ' · resumo final' : ' · parcial')}
          </p>
        </div>
        <div className={styles.weekNav}>
          <button
            type="button"
            className={styles.navButton}
            aria-label="Semana anterior"
            disabled={weekStart <= firstWeek}
            onClick={() => setWeekStart(addDays(weekStart, -7))}
          >
            <ChevronLeftIcon />
          </button>
          <button
            type="button"
            className={styles.navButton}
            aria-label="Semana seguinte"
            disabled={isCurrent}
            onClick={() => setWeekStart(addDays(weekStart, 7))}
          >
            <ChevronRightIcon />
          </button>
        </div>
      </div>

      {summary.isPending && <p className={styles.note}>Carregando a semana…</p>}
      {summary.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(summary.error).message}</p>
          <Button variant="secondary" onClick={() => void summary.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}
      {summary.data && <WeekSummaryBody summary={summary.data} stale={summary.isPlaceholderData} />}

      {isCurrent && (
        <ButtonLink to="/semana?semana=proxima" variant="secondary" block>
          Planejar a próxima semana
        </ButtonLink>
      )}
    </section>
  )
}

function WeekSummaryBody({ summary, stale }: { summary: WeekSummary; stale: boolean }) {
  const { totals } = summary
  return (
    <div className={styles.summaryBody} data-stale={stale || undefined} aria-busy={stale || undefined}>
      <p className={styles.headline}>
        {totals.planned === 0 ? (
          'Nenhuma tarefa planejada nessa semana.'
        ) : (
          <>
            <strong className={styles.headlineValue}>
              {totals.done} de {totals.planned}
            </strong>{' '}
            tarefas concluídas
            {totals.completionRate !== null && <span className={styles.rate}>{totals.completionRate}%</span>}
          </>
        )}
      </p>

      <ol className={styles.days} aria-label="Dias da semana">
        {summary.days.map((day) => {
          const status = DAY_STATUS[day.status]
          const counts =
            day.totals.mandatoryPlanned > 0 ? `${day.totals.mandatoryDone}/${day.totals.mandatoryPlanned}` : ''
          return (
            <li key={day.date} className={styles.day} data-status={day.status}>
              <span className={styles.dayName} aria-hidden="true">
                {shortDay(day.dayOfWeek)}
              </span>
              <span className={styles.dayMark} aria-hidden="true" title={status.label}>
                {status.icon}
              </span>
              <span className={styles.dayCount} aria-hidden="true">
                {counts}
              </span>
              <span className="visually-hidden">
                {longDay(day.dayOfWeek)}, {formatShortDate(day.date)}: {status.label}
                {counts && `, ${day.totals.mandatoryDone} de ${day.totals.mandatoryPlanned} obrigatórias`}
              </span>
            </li>
          )
        })}
      </ol>

      <dl className={styles.figures}>
        <Figure label="Dias cumpridos" value={summary.fulfilledDays} />
        <Figure label="Perdidas" value={totals.missed} />
        <Figure label="Pontos" value={totals.points} />
        <Figure label="Moedas" value={totals.coins} />
      </dl>

      {summary.achievements.length > 0 && (
        <div className={styles.achievements}>
          <h3 className={styles.subTitle}>Conquistas da semana</h3>
          <ul className={styles.achievementList}>
            {summary.achievements.map((achievement) => (
              <li key={achievement.code} className={styles.achievement}>
                <TrophyIcon aria-hidden="true" />
                {achievement.name}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}

function Figure({ label, value }: { label: string; value: number }) {
  return (
    <div className={styles.figure}>
      <dt>{label}</dt>
      <dd>{value.toLocaleString('pt-BR')}</dd>
    </div>
  )
}

// ------------------------------------------------------------------ histórico

const GRANULARITIES = [
  { value: 'WEEK', label: 'Semanas' },
  { value: 'MONTH', label: 'Meses' },
] as const

function HistorySection() {
  const [granularity, setGranularity] = useState<StatsGranularity>('WEEK')
  const history = useStatsHistory(granularity, granularity === 'WEEK' ? 8 : 6)

  return (
    <section className={styles.section} aria-labelledby="history-title">
      <h2 id="history-title" className={styles.sectionTitle}>
        Planejado × concluído
      </h2>
      <ToggleGroup label="Agrupar por" value={granularity} options={GRANULARITIES} onChange={setGranularity} />
      {history.isPending && <p className={styles.note}>Carregando o histórico…</p>}
      {history.isError && (
        <p className={styles.error} role="alert">
          {asApiError(history.error).message}
        </p>
      )}
      {history.data && (
        <PlannedDoneChart
          granularity={history.data.granularity}
          periods={history.data.periods}
          stale={history.isPlaceholderData}
        />
      )}
    </section>
  )
}

// ------------------------------------------------------------------ desde o começo

function OverviewSection() {
  const overview = useStatsOverview()
  return (
    <section className={styles.section} aria-labelledby="overview-title">
      <h2 id="overview-title" className={styles.sectionTitle}>
        Desde o começo
      </h2>
      {overview.isPending && <p className={styles.note}>Carregando…</p>}
      {overview.isError && (
        <p className={styles.error} role="alert">
          {asApiError(overview.error).message}
        </p>
      )}
      {overview.data && <OverviewBody overview={overview.data} />}
    </section>
  )
}

function OverviewBody({ overview }: { overview: StatsOverview }) {
  const top = Math.max(...overview.completedByCategory.map((category) => category.completed), 1)
  return (
    <>
      <dl className={styles.tiles}>
        <Tile label="Tarefas concluídas" value={overview.completedTasks} detail={plural(overview.missedTasks, 'perdida', 'perdidas')} />
        <Tile
          label="Taxa de conclusão"
          value={overview.completionRate === null ? '—' : `${overview.completionRate}%`}
          detail="das tarefas já encerradas"
        />
        <Tile label="Pontos" value={overview.points} detail={plural(overview.achievementsUnlocked, 'conquista desbloqueada', 'conquistas desbloqueadas')} />
        <Tile
          label="Maior sequência"
          value={plural(overview.longestStreak, 'dia', 'dias')}
          detail={`agora: ${plural(overview.currentStreak, 'dia', 'dias')}`}
        />
        <Tile label="Moedas ganhas" value={overview.coinsEarned} detail={`${overview.coinsSpent.toLocaleString('pt-BR')} gastas na loja`} />
        <Tile
          label="Dias cumpridos"
          value={overview.fulfilledDays}
          detail={`${plural(overview.failedDays, 'falha', 'falhas')}, ${overview.restDays} de descanso`}
        />
      </dl>

      <h3 className={styles.subTitle}>Concluídas por categoria</h3>
      {overview.completedByCategory.length === 0 ? (
        <p className={styles.note}>Conclua tarefas para ver onde você mais investe o seu tempo.</p>
      ) : (
        <ul className={styles.categories}>
          {overview.completedByCategory.map(({ category, completed }) => (
            <li key={category} className={styles.category} style={{ '--tape': categoryColor(category) } as CSSProperties}>
              <span className={styles.categoryName}>{categoryLabel(category)}</span>
              <span className={styles.categoryTrack} aria-hidden="true">
                <span style={{ width: `${(completed / top) * 100}%` }} />
              </span>
              <span className={styles.categoryValue}>{completed.toLocaleString('pt-BR')}</span>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

function Tile({ label, value, detail }: { label: string; value: number | string; detail?: string }) {
  return (
    <div className={styles.tile}>
      <dt className={styles.tileLabel}>{label}</dt>
      <dd className={styles.tileValue}>{typeof value === 'number' ? value.toLocaleString('pt-BR') : value}</dd>
      {detail && <dd className={styles.tileDetail}>{detail}</dd>}
    </div>
  )
}
