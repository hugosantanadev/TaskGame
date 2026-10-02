import { useState } from 'react'
import { Link } from 'react-router'

import { asApiError } from '../../api/errors'
import type { Ranking, RankingMetric } from '../../api/types'
import { Button } from '../../components/Button'
import { PageTitle } from '../../components/PageTitle'
import { ToggleGroup } from '../../components/ToggleGroup'
import { formatShortDate, plural } from '../../lib/days'
import styles from './RankingPage.module.css'
import { useRanking } from './rankingApi'

const METRICS = [
  { value: 'POINTS', label: 'Pontos' },
  { value: 'COMPLETED_TASKS', label: 'Tarefas' },
  { value: 'COINS_EARNED', label: 'Moedas' },
  { value: 'STREAK', label: 'Sequência' },
] as const

const UNITS: Record<RankingMetric, [string, string]> = {
  POINTS: ['ponto', 'pontos'],
  COMPLETED_TASKS: ['tarefa', 'tarefas'],
  COINS_EARNED: ['moeda', 'moedas'],
  STREAK: ['dia', 'dias'],
}

function valueOf(metric: RankingMetric, value: number): string {
  const [one, many] = UNITS[metric]
  return plural(value, one, many)
}

export function RankingPage() {
  const [metric, setMetric] = useState<RankingMetric>('POINTS')
  const ranking = useRanking(metric)
  const first = ranking.data?.pages[0]
  const entries = ranking.data?.pages.flatMap((page) => page.entries.content) ?? []
  const stale = ranking.isPlaceholderData

  return (
    <div className={styles.page}>
      <PageTitle title="Ranking" />
      <div>
        <h1 className={styles.heading}>Ranking</h1>
        {first && (
          <p className={styles.range}>
            Semana de {formatShortDate(first.from)} a {formatShortDate(first.to)}
          </p>
        )}
      </div>
      <ToggleGroup label="Ordenar por" value={metric} options={METRICS} onChange={setMetric} />

      {ranking.isPending && <p className={styles.note}>Carregando o ranking…</p>}
      {ranking.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(ranking.error).message}</p>
          <Button variant="secondary" onClick={() => void ranking.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}

      {first && (
        <div className={styles.body} data-stale={stale || undefined} aria-busy={stale || undefined}>
          <MyPosition ranking={first} />

          {entries.length === 0 ? (
            <p className={styles.note}>Ninguém pontuou nesta semana ainda.</p>
          ) : (
            <ol className={styles.list} aria-label={`Ranking por ${METRICS.find((m) => m.value === first.metric)?.label}`}>
              {entries.map((entry) => (
                <li
                  key={`${entry.position}-${entry.displayName}-${entry.you}`}
                  className={styles.row}
                  data-you={entry.you || undefined}
                  data-podium={entry.position <= 3 ? entry.position : undefined}
                >
                  <span className={styles.position}>
                    <span className="visually-hidden">Posição </span>
                    {entry.position}
                  </span>
                  <span className={styles.name}>
                    <span className={styles.nameText}>{entry.displayName}</span>
                    {entry.you && <span className={styles.youTag}>você</span>}
                  </span>
                  <span className={styles.value}>{valueOf(first.metric, entry.value)}</span>
                </li>
              ))}
            </ol>
          )}

          {ranking.hasNextPage && (
            <Button variant="secondary" disabled={ranking.isFetchingNextPage} onClick={() => void ranking.fetchNextPage()}>
              {ranking.isFetchingNextPage ? 'Carregando…' : 'Ver mais'}
            </Button>
          )}
        </div>
      )}
    </div>
  )
}

function MyPosition({ ranking }: { ranking: Ranking }) {
  const { me, metric } = ranking
  return (
    <section className={styles.me} aria-label="Sua posição">
      {me.position === null ? (
        <p className={styles.meText}>
          {metric === 'STREAK'
            ? 'Cumpra todas as obrigatórias de um dia para começar uma sequência e entrar no ranking.'
            : 'Conclua tarefas nesta semana para entrar no ranking.'}
        </p>
      ) : (
        <p className={styles.meText}>
          <span className={styles.mePosition}>{me.position}º</span>
          <span>
            {me.visible ? 'Sua posição' : 'Onde você estaria'} · <strong>{valueOf(metric, me.value)}</strong>
          </span>
        </p>
      )}
      {!me.visible && (
        <p className={styles.meHint}>
          Seu nome não aparece no ranking para outras pessoas. <Link to="/perfil">Mudar no perfil</Link>
        </p>
      )}
    </section>
  )
}
