import { useState, type CSSProperties } from 'react'

import { asApiError } from '../../api/errors'
import type { CharacterView, MissionEvolution } from '../../api/types'
import { ButtonLink } from '../../components/Button'
import { PixelIcon } from '../../game/pixel/PixelSprite'
import { ATTRIBUTE_LABELS, attributeColor, levelProgress } from '../../lib/attributes'
import { categoryColor, categoryLabel } from '../../lib/categories'
import { formatMonth, formatShortDate, plural } from '../../lib/days'
import { bestTitle, isMasterTitle, nextTitle, titleLabel } from '../../lib/titles'
import { useCharacter } from '../store/storeApi'
import { defaultMission } from './missionPick'
import styles from './EvolutionSections.module.css'
import { PlannedDoneChart } from './PlannedDoneChart'
import { useMissionEvolution, useMissionStats } from './statsApi'

/** Evolução por missão: escolha uma (ex.: Academia) e veja semana a semana quantas vezes ela foi feita. */
export function MissionEvolutionSection() {
  const missions = useMissionStats()
  const [chosen, setChosen] = useState<string | null>(null)
  const list = missions.data ?? []
  const selected = list.find((mission) => mission.taskId === chosen) ?? defaultMission(list)
  const evolution = useMissionEvolution(selected?.taskId ?? null)

  return (
    <section className={styles.card} aria-labelledby="mission-evolution-title">
      <div>
        <h2 id="mission-evolution-title" className={styles.title}>
          Evolução por missão
        </h2>
        <p className={styles.note}>Quantas vezes você foi à academia, estudou ou leu em cada semana.</p>
      </div>

      {missions.isPending && <p className={styles.note}>Carregando as missões…</p>}
      {missions.isError && (
        <p className={styles.error} role="alert">
          {asApiError(missions.error).message}
        </p>
      )}
      {missions.data && list.length === 0 && (
        <div className={styles.empty}>
          <p>Crie uma missão para acompanhar a evolução dela aqui.</p>
          <ButtonLink to="/missoes/nova" variant="secondary">
            Criar missão
          </ButtonLink>
        </div>
      )}

      {list.length > 0 && selected && (
        <>
          <ul className={styles.missions} aria-label="Escolha a missão">
            {[...list]
              .sort((a, b) => Number(a.archived) - Number(b.archived) || b.completed - a.completed)
              .map((mission) => (
                <li key={mission.taskId}>
                  <button
                    type="button"
                    className={styles.mission}
                    style={{ '--tape': categoryColor(mission.category) } as CSSProperties}
                    aria-pressed={mission.taskId === selected.taskId}
                    onClick={() => setChosen(mission.taskId)}
                  >
                    {mission.name}
                    {mission.archived && <span className={styles.archived}> (arquivada)</span>}
                  </button>
                </li>
              ))}
          </ul>
          {evolution.isError && (
            <p className={styles.error} role="alert">
              {asApiError(evolution.error).message}
            </p>
          )}
          {evolution.data && <EvolutionBody evolution={evolution.data} stale={evolution.isPlaceholderData} />}
        </>
      )}
    </section>
  )
}

function EvolutionBody({ evolution, stale }: { evolution: MissionEvolution; stale: boolean }) {
  const { summary } = evolution
  const average = evolution.weeklyAverage.toLocaleString('pt-BR', { maximumFractionDigits: 1 })
  return (
    <div className={styles.body} data-stale={stale || undefined} aria-busy={stale || undefined}>
      <p className={styles.headline}>
        <span className={styles.category} style={{ '--tape': categoryColor(summary.category) } as CSSProperties}>
          {categoryLabel(summary.category)}
        </span>
        {summary.completed === 0
          ? 'Ainda não foi concluída.'
          : `Feita ${plural(summary.completed, 'vez', 'vezes')}${
              evolution.firstCompletedDate ? ` desde ${formatShortDate(evolution.firstCompletedDate)}` : ''
            }.`}
      </p>
      <dl className={styles.stats}>
        <Stat icon="flame" label="Sequência" value={plural(summary.currentStreak, 'seguida', 'seguidas')} detail={`recorde: ${summary.bestStreak}`} />
        <Stat
          icon="star"
          label="Taxa"
          value={summary.completionRate === null ? '—' : `${summary.completionRate}%`}
          detail={plural(summary.missed, 'perdida', 'perdidas')}
        />
        <Stat icon="gem" label="Por semana" value={average} detail="média das últimas 12" />
        <Stat
          icon="coin"
          label="Melhor mês"
          value={evolution.bestMonth ? formatMonth(evolution.bestMonth) : '—'}
          detail={evolution.bestMonth ? plural(evolution.bestMonthCompleted, 'vez', 'vezes') : 'sem conclusões'}
        />
      </dl>
      <PlannedDoneChart granularity="WEEK" periods={evolution.weeks} stale={stale} />
    </div>
  )
}

function Stat({ icon, label, value, detail }: { icon: 'flame' | 'star' | 'gem' | 'coin'; label: string; value: string; detail: string }) {
  return (
    <div className={styles.stat}>
      <dt>
        <PixelIcon name={icon} /> {label}
      </dt>
      <dd className={styles.statValue}>{value}</dd>
      <dd className={styles.statDetail}>{detail}</dd>
    </div>
  )
}

/** O treino de cada atributo: nível, barra e o próximo título ("Mestre nos estudos no nível 10"). */
export function TrainingSection() {
  const character = useCharacter()
  if (!character.data) return null
  return <TrainingBody character={character.data} />
}

function TrainingBody({ character }: { character: CharacterView }) {
  const trained = [...character.attributes].sort((a, b) => b.xp - a.xp)
  return (
    <section className={styles.card} aria-labelledby="training-title">
      <div>
        <h2 id="training-title" className={styles.title}>
          Seu treino
        </h2>
        <p className={styles.note}>Cada tarefa concluída treina um atributo. Os títulos chegam nos níveis 3, 6 e 10.</p>
      </div>
      <ul className={styles.training}>
        {trained.map((status) => {
          const best = bestTitle(character.titles, status.attribute)
          const next = nextTitle(character.titles, status.attribute)
          const name = ATTRIBUTE_LABELS[status.attribute]
          return (
            <li key={status.attribute} className={styles.attribute} style={{ '--attribute': attributeColor(status) } as CSSProperties}>
              <span className={styles.level} aria-hidden="true">
                {status.level}
              </span>
              <span className={styles.attributeText}>
                <span className={styles.attributeName}>
                  {name}
                  <span className="visually-hidden">, nível {status.level}</span>
                  {best && (
                    <span className={styles.badge} data-master={isMasterTitle(best.code) || undefined}>
                      {titleLabel(best.code)}
                    </span>
                  )}
                </span>
                <span
                  className={styles.bar}
                  role="progressbar"
                  aria-label={`${name}: progresso do nível`}
                  aria-valuemin={status.levelStartXp}
                  aria-valuemax={status.nextLevelXp}
                  aria-valuenow={status.xp}
                >
                  <span style={{ width: `${levelProgress(status) * 100}%` }} />
                </span>
                <span className={styles.next}>
                  {next ? `${titleLabel(next.code)} no nível ${next.level}` : 'Todos os títulos ganhos'}
                </span>
              </span>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
