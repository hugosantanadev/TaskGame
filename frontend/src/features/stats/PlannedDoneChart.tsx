import { useState } from 'react'

import type { HistoryPeriod, StatsGranularity } from '../../api/types'
import { formatDayMonth, formatMonth, formatMonthShort, formatShortDate, plural } from '../../lib/days'
import styles from './PlannedDoneChart.module.css'

type Props = {
  granularity: StatsGranularity
  periods: HistoryPeriod[]
  /** Dados do agrupamento anterior enquanto o novo carrega: o gráfico fica, esmaecido. */
  stale?: boolean
}

const NICE_STEPS = [1, 2, 3, 4, 5, 6, 8, 10]

/** Passo "redondo" do eixo (1, 2, 5, 10, 20…), nunca menor que 1: as contagens são inteiras. */
function niceStep(raw: number): number {
  const magnitude = 10 ** Math.floor(Math.log10(raw))
  const step = NICE_STEPS.find((nice) => nice * magnitude >= raw) ?? 10
  return Math.max(1, step * magnitude)
}

function periodTitle(period: HistoryPeriod, granularity: StatsGranularity): string {
  const title =
    granularity === 'WEEK'
      ? `Semana de ${formatShortDate(period.start)} a ${formatShortDate(period.end)}`
      : formatMonth(period.start)
  return period.current ? `${title} (em andamento)` : title
}

function tickLabel(period: HistoryPeriod, granularity: StatsGranularity): string {
  return granularity === 'WEEK' ? formatDayMonth(period.start) : formatMonthShort(period.start)
}

/**
 * Colunas de planejado × concluído: cada coluna é o planejado do período e a parte de baixo, mais escura,
 * o que foi concluído. Tocar, focar ou passar o mouse numa coluna mostra os números dela abaixo do gráfico;
 * a tabela traz todos os valores sem depender disso.
 */
export function PlannedDoneChart({ granularity, periods, stale = false }: Props) {
  const [selected, setSelected] = useState<string | null>(null)
  const active = periods.find((period) => period.start === selected) ?? periods[periods.length - 1]
  const step = niceStep(Math.max(...periods.map((period) => period.totals.planned), 1) / 2)
  const max = step * 2
  const ticks = [0, step, max]
  const unit = granularity === 'WEEK' ? 'semana' : 'mês'

  if (periods.every((period) => period.totals.planned === 0)) {
    return <p className={styles.empty}>Ainda não há tarefas planejadas nesse período.</p>
  }

  return (
    <figure className={styles.figure} data-stale={stale || undefined} aria-busy={stale || undefined}>
      <ul className={styles.legend} aria-label="Legenda">
        <li>
          <span className={styles.keyDone} aria-hidden="true" /> Concluídas
        </li>
        <li>
          <span className={styles.keyPlanned} aria-hidden="true" /> Planejadas
        </li>
      </ul>

      <div className={styles.plot}>
        <div className={styles.yAxis} aria-hidden="true">
          {ticks.map((tick) => (
            <span key={tick} style={{ bottom: `${(tick / max) * 100}%` }}>
              {tick}
            </span>
          ))}
        </div>
        <div className={styles.area}>
          <div className={styles.gridlines} aria-hidden="true">
            {ticks.map((tick) => (
              <span key={tick} data-base={tick === 0 || undefined} style={{ bottom: `${(tick / max) * 100}%` }} />
            ))}
          </div>
          <ol className={styles.columns} aria-label={`Planejado e concluído por ${unit}`}>
            {periods.map((period) => {
              const { planned, done } = period.totals
              const isActive = period === active
              return (
                <li key={period.start} className={styles.slot}>
                  <button
                    type="button"
                    className={styles.column}
                    aria-pressed={isActive}
                    aria-label={`${periodTitle(period, granularity)}: ${done} de ${planned} concluídas`}
                    onClick={() => setSelected(period.start)}
                    onFocus={() => setSelected(period.start)}
                    onPointerEnter={(event) => event.pointerType === 'mouse' && setSelected(period.start)}
                  >
                    {planned > 0 && isActive && (
                      <span
                        className={styles.cap}
                        style={{ bottom: `calc(${(planned / max) * 100}% + 0.25rem)` }}
                        aria-hidden="true"
                      >
                        {done}/{planned}
                      </span>
                    )}
                    {planned > 0 && (
                      <span className={styles.stack} style={{ height: `${(planned / max) * 100}%` }}>
                        {planned > done && <span className={styles.planned} style={{ flexGrow: planned - done }} />}
                        {done > 0 && <span className={styles.done} style={{ flexGrow: done }} />}
                      </span>
                    )}
                  </button>
                  <span className={styles.tick} data-active={isActive || undefined} aria-hidden="true">
                    {tickLabel(period, granularity)}
                  </span>
                </li>
              )
            })}
          </ol>
        </div>
      </div>

      {active && (
        <figcaption className={styles.readout} aria-live="polite">
          <span className={styles.readoutValue}>
            {active.totals.planned === 0
              ? 'Nada planejado'
              : `${active.totals.done} de ${active.totals.planned} concluídas${
                  active.totals.completionRate === null ? '' : ` (${active.totals.completionRate}%)`
                }`}
          </span>
          <span className={styles.readoutLabel}>{periodTitle(active, granularity)}</span>
          {active.totals.planned > 0 && (
            <span className={styles.readoutDetail}>
              {plural(active.totals.missed, 'perdida', 'perdidas')} · {plural(active.totals.points, 'ponto', 'pontos')} ·{' '}
              {plural(active.totals.coins, 'moeda', 'moedas')}
            </span>
          )}
        </figcaption>
      )}

      <details className={styles.tableView}>
        <summary>Ver em tabela</summary>
        <div className={styles.tableScroll}>
          <table>
            <thead>
              <tr>
                <th scope="col">{granularity === 'WEEK' ? 'Semana' : 'Mês'}</th>
                <th scope="col">Planejadas</th>
                <th scope="col">Concluídas</th>
                <th scope="col">Perdidas</th>
                <th scope="col">Taxa</th>
                <th scope="col">Pontos</th>
              </tr>
            </thead>
            <tbody>
              {periods.map((period) => (
                <tr key={period.start}>
                  <th scope="row">
                    {granularity === 'WEEK' ? formatShortDate(period.start) : formatMonth(period.start)}
                    {period.current && ' (atual)'}
                  </th>
                  <td>{period.totals.planned}</td>
                  <td>{period.totals.done}</td>
                  <td>{period.totals.missed}</td>
                  <td>{period.totals.completionRate === null ? '—' : `${period.totals.completionRate}%`}</td>
                  <td>{period.totals.points}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </details>
    </figure>
  )
}
