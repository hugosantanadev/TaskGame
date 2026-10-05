import { useEffect, useState } from 'react'
import { Link } from 'react-router'

import { asApiError } from '../../api/errors'
import type { Occurrence, Period, Today } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { PlusIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { RankBadge } from '../../components/RankBadge'
import { wornCodes } from '../../game/pixel/items'
import { PixelIcon } from '../../game/pixel/PixelSprite'
import { readSeenRank, writeSeenRank } from '../../game/rankMemory'
import { publishReward } from '../../game/rewardFeedback'
import { sameRank } from '../../lib/rank'
import { DAY_PERIODS, describeDay, hourIn, safeTimeZone, timeOfDay } from '../../lib/datetime'
import { addDays, formatTime, minutesOf, plural, weekStartOf } from '../../lib/days'
import { useNow } from '../../lib/useNow'
import { ChestCard, ChestProgress } from '../chest/ChestCard'
import { useWeekSummary } from '../stats/statsApi'
import { useCharacter } from '../store/storeApi'
import { ChallengesPanel } from './ChallengesPanel'
import { CompleteSheet } from './CompleteSheet'
import { DayTabs } from './DayTabs'
import { OccurrenceRow } from './OccurrenceRow'
import { TodayHero } from './TodayHero'
import styles from './TodayPage.module.css'
import { useToday } from './todayApi'

const GREETINGS: Record<Period, string> = {
  MORNING: 'Bom dia',
  AFTERNOON: 'Boa tarde',
  SUNSET: 'Boa tarde',
  NIGHT: 'Boa noite',
}

export function TodayPage() {
  const user = useCurrentUser()
  const now = useNow()
  const zone = safeTimeZone(user.timeZone)
  const today = useToday()
  const character = useCharacter()
  const [selected, setSelected] = useState<Occurrence | null>(null)
  const day = describeDay(now, zone)
  const period: Period = timeOfDay(hourIn(zone, now))
  const firstName = user.displayName.trim().split(/\s+/)[0]
  const data = today.data
  // Mostra no painel a versão mais nova da tarefa (depois de concluir, a lista é recarregada)
  const current = selected && data ? (data.occurrences.find((o) => o.id === selected.id) ?? selected) : selected
  const rank = data?.rank

  // Elo que mudou sem uma conclusão no meio (caiu na virada do dia): avisa uma vez
  useEffect(() => {
    if (!rank) return
    const seen = readSeenRank(user.id)
    writeSeenRank(user.id, rank)
    if (seen && !sameRank(seen, rank)) publishReward({ kind: 'rank', from: seen, to: rank })
  }, [rank, user.id])

  return (
    <div className={styles.page}>
      <PageTitle title="Hoje" />
      <TodayHero
        period={period}
        greeting={`${GREETINGS[period]}, ${firstName}.`}
        day={day}
        wearing={wornCodes(character.data)}
        state={character.data?.state ?? 'IDLE'}
        title={character.data?.activeTitle ?? user.activeTitle}
      />
      {data && (
        <ul className={styles.chips} aria-label="Seu placar">
          <li className={styles.rankChip}>
            <Link to="/ranking/elo" className={styles.rankLink} aria-label="Seu elo: ver elos e recompensas">
              <RankBadge rank={data.rank} size="s" />
            </Link>
          </li>
          <li className={styles.streakChip}>
            <PixelIcon name="flame" /> {data.streak.current}
            <span className="visually-hidden"> {data.streak.current === 1 ? 'dia' : 'dias'} de sequência</span>
            {data.streak.freezes > 0 && (
              <span className={styles.shields}>
                <PixelIcon name="shield" /> {data.streak.freezes}
                <span className="visually-hidden">
                  {' '}
                  {data.streak.freezes === 1 ? 'protetor guardado' : 'protetores guardados'}
                </span>
              </span>
            )}
          </li>
          <li className={styles.coinChip}>
            <PixelIcon name="coin" /> {data.walletBalance}
            <span className="visually-hidden"> moedas</span>
          </li>
          <li className={styles.pointChip}>
            <PixelIcon name="star" /> {data.progress.points}
            <span className="visually-hidden"> pontos hoje</span>
          </li>
        </ul>
      )}
      {data && data.streak.lastFrozenDate === addDays(data.date, -1) && (
        <p className={styles.frozenNote}>
          <PixelIcon name="shield" /> Seu protetor salvou a sequência ontem.
        </p>
      )}

      <DayTabs />

      {today.isPending && <p className={styles.loading}>Carregando o dia…</p>}
      {today.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(today.error).message}</p>
          <Button variant="secondary" onClick={() => void today.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}
      {data && <DayContent data={data} period={period} onSelect={setSelected} />}

      <CompleteSheet occurrence={current} isToday onClose={() => setSelected(null)} />
    </div>
  )
}

function DayContent({ data, period, onSelect }: { data: Today; period: Period; onSelect: (o: Occurrence) => void }) {
  const next = data.occurrences.find((o) => o.id === data.nextOccurrenceId) ?? null
  const untimed = data.occurrences.filter((o) => !o.plannedTime)

  return (
    <>
      {data.pendingChest && <ChestCard chest={data.pendingChest} />}

      <ProgressPanel data={data} />

      {next && (
        <button type="button" className={styles.next} onClick={() => onSelect(next)} aria-haspopup="dialog">
          <span className={styles.nextLabel}>Próxima</span>
          <span className={styles.nextTitle}>{next.title}</span>
          <span className={styles.nextTime}>{formatTime(next.plannedTime) ?? 'sem horário'}</span>
        </button>
      )}

      <section className={styles.agenda} aria-labelledby="agenda-title">
        <h2 id="agenda-title" className="visually-hidden">
          Agenda de hoje
        </h2>
        {DAY_PERIODS.map((slot) => {
          const items = inPeriod(data.occurrences, slot.id)
          return (
            <div key={slot.id} className={styles.period} data-current={slot.id === period || undefined}>
              <h3 className={styles.periodHead}>
                <span className={styles.periodStart}>{slot.start}</span>
                <span className={styles.periodName}>
                  {slot.label}
                  {slot.id === period && <span className={styles.now}>agora</span>}
                </span>
              </h3>
              {items.length > 0 && (
                <ul className={styles.list}>
                  {items.map((o) => (
                    <OccurrenceRow key={o.id} occurrence={o} next={o.id === next?.id} onSelect={onSelect} />
                  ))}
                </ul>
              )}
            </div>
          )
        })}
        {untimed.length > 0 && (
          <div className={styles.period}>
            <h3 className={styles.periodHead}>
              <span className={styles.periodStart} />
              <span className={styles.periodName}>Sem horário</span>
            </h3>
            <ul className={styles.list}>
              {untimed.map((o) => (
                <OccurrenceRow key={o.id} occurrence={o} next={o.id === next?.id} onSelect={onSelect} />
              ))}
            </ul>
          </div>
        )}
        {data.occurrences.length === 0 && (
          <div className={styles.empty}>
            <p className={styles.emptyTitle}>Nenhuma tarefa para hoje.</p>
            <p className={styles.emptyText}>
              {data.onboarding
                ? 'É seu primeiro dia: o que você criar agora já pode entrar no plano de hoje.'
                : 'Crie uma missão ou inclua uma das suas no dia de hoje pela aba Semana.'}
            </p>
          </div>
        )}
      </section>

      <ChallengesPanel challenges={data.challenges} />

      <WeekChest date={data.date} />

      <div className={styles.actions}>
        <ButtonLink to="/missoes/nova">
          <PlusIcon /> Nova missão
        </ButtonLink>
        <ButtonLink to="/missoes" variant="secondary">
          Minhas missões
        </ButtonLink>
      </div>
    </>
  )
}

/** O baú que esta semana está valendo, para dar vontade de cumprir mais um dia. */
function WeekChest({ date }: { date: string }) {
  const summary = useWeekSummary(weekStartOf(date))
  return summary.data ? <ChestProgress summary={summary.data} /> : null
}

function ProgressPanel({ data }: { data: Today }) {
  const { mandatoryPlanned: planned, mandatoryDone: done } = data.progress
  const missing = planned - done
  let message: string
  if (data.streak.todayStatus === 'REST') message = 'Dia de descanso: sem obrigatórias, a sequência fica como está.'
  else if (data.streak.todayStatus === 'FULFILLED') message = `Dia cumprido. Sequência de ${plural(data.streak.current, 'dia', 'dias')}.`
  else message = `${missing === 1 ? 'Falta 1 obrigatória' : `Faltam ${missing} obrigatórias`} para manter a sequência.`

  return (
    <section className={styles.progress} data-status={data.streak.todayStatus} aria-labelledby="progress-title">
      <h2 id="progress-title" className={styles.progressTitle}>
        {message}
      </h2>
      {planned > 0 && (
        <>
          <div
            className={styles.bar}
            role="progressbar"
            aria-label="Obrigatórias concluídas"
            aria-valuemin={0}
            aria-valuemax={planned}
            aria-valuenow={done}
          >
            <span style={{ width: `${(done / planned) * 100}%` }} />
          </div>
          <p className={styles.progressText}>
            {done} de {plural(planned, 'obrigatória', 'obrigatórias')}
            {data.progress.extrasPlanned > 0 &&
              `, ${data.progress.extrasDone} de ${plural(data.progress.extrasPlanned, 'extra', 'extras')}`}
          </p>
        </>
      )}
    </section>
  )
}

/** Agrupa pelo período do horário planejado; na noite, 19h–23h vêm antes da madrugada. */
function inPeriod(occurrences: Occurrence[], period: Period): Occurrence[] {
  const key = (o: Occurrence) => {
    const minutes = minutesOf(o.plannedTime ?? '00:00')
    return minutes < 300 ? minutes + 1440 : minutes
  }
  return occurrences
    .filter((o) => o.plannedTime && timeOfDay(Number(o.plannedTime.slice(0, 2))) === period)
    .sort((a, b) => key(a) - key(b))
}
