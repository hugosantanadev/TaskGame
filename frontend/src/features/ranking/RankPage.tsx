import { asApiError } from '../../api/errors'
import type { Progression, RankTier, XpReason } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { LockIcon, ShirtIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { RankBadge } from '../../components/RankBadge'
import { safeTimeZone } from '../../lib/datetime'
import { formatShortDate, todayIso } from '../../lib/days'
import { rankLabel, rankProgress, sameRank, TIER_LABELS } from '../../lib/rank'
import styles from './RankPage.module.css'
import { useMyRank } from './rankingApi'

const REASONS: Record<XpReason, string> = {
  TASK_COMPLETED: 'Tarefa concluída',
  DAY_FULFILLED: 'Dia cumprido',
  TASK_MISSED: 'Obrigatória perdida',
  BACKFILL: 'XP do seu histórico',
}

/** Elos e recompensas: onde você está, a escada inteira, as roupas de cada elo e o extrato de XP. */
export function RankPage() {
  const rank = useMyRank()

  return (
    <div className={styles.page}>
      <PageTitle title="Elos" />
      <ButtonLink to="/ranking" variant="quiet" className={styles.back}>
        Voltar ao ranking
      </ButtonLink>
      <h1 className={styles.heading}>Elos e recompensas</h1>
      {rank.isPending && <p className={styles.note}>Carregando…</p>}
      {rank.isError && (
        <div className={styles.failure} role="alert">
          <p>{asApiError(rank.error).message}</p>
          <Button variant="secondary" onClick={() => void rank.refetch()}>
            Tentar de novo
          </Button>
        </div>
      )}
      {rank.data && <RankDetails progression={rank.data} />}
    </div>
  )
}

function RankDetails({ progression }: { progression: Progression }) {
  const { status, peakRank } = progression
  const zone = safeTimeZone(useCurrentUser().timeZone)
  const toNext = status.nextRankXp === null ? null : status.nextRankXp - status.xp

  return (
    <>
      <section className={styles.hero} aria-label="Seu elo">
        <RankBadge rank={status} size="l" />
        <div
          className={styles.bar}
          role="progressbar"
          aria-label="XP até o próximo degrau"
          aria-valuemin={status.rankStartXp}
          aria-valuemax={status.nextRankXp ?? status.xp}
          aria-valuenow={status.xp}
        >
          <span style={{ width: `${rankProgress(status) * 100}%` }} />
        </div>
        <p className={styles.heroText}>
          <strong>{status.xp} XP</strong>
          {toNext === null ? ' · você está no topo' : ` · faltam ${toNext} para o próximo degrau`}
        </p>
        {!sameRank(peakRank, status) && (
          <p className={styles.note}>Seu maior elo foi {rankLabel(peakRank)}. As roupas dele continuam suas.</p>
        )}
        <p className={styles.note}>
          Cada tarefa concluída rende os pontos dela em XP, e o dia cumprido dá um bônus. Cada obrigatória perdida tira XP.
        </p>
      </section>

      <section className={styles.section} aria-labelledby="rank-rewards">
        <h2 id="rank-rewards" className={styles.sectionTitle}>
          Roupas de elo
        </h2>
        <p className={styles.note}>Ninguém compra: só quem chega ao elo ganha. Vista no seu personagem.</p>
        <ul className={styles.rewards}>
          {progression.rewards.map(({ tier, item }) => (
            <li key={tier} className={styles.reward} data-owned={item.owned || undefined}>
              <span className={styles.rewardIcon} aria-hidden="true">
                {item.owned ? <ShirtIcon /> : <LockIcon />}
              </span>
              <span className={styles.rewardText}>
                <span className={styles.rewardName}>{item.name}</span>
                <span className={styles.rewardTier}>
                  {item.owned ? 'Desbloqueada' : `Chegue ${toTier(tier)}`}
                </span>
              </span>
            </li>
          ))}
        </ul>
        <ButtonLink to="/perfil/personagem" variant="secondary">
          Vestir o personagem
        </ButtonLink>
      </section>

      <section className={styles.section} aria-labelledby="rank-ladder">
        <h2 id="rank-ladder" className={styles.sectionTitle}>
          A escada
        </h2>
        <ol className={styles.ladder}>
          {[...progression.ladder].reverse().map((step) => {
            const current = sameRank(step.rank, status)
            return (
              <li key={`${step.rank.tier}-${step.rank.division}`} className={styles.step} data-current={current || undefined}>
                <RankBadge rank={step.rank} size="s" />
                <span className={styles.stepXp}>{step.minXp} XP</span>
                {current && <span className={styles.youTag}>você</span>}
              </li>
            )
          })}
        </ol>
      </section>

      <section className={styles.section} aria-labelledby="rank-history">
        <h2 id="rank-history" className={styles.sectionTitle}>
          Últimas mudanças de XP
        </h2>
        {progression.recent.length === 0 ? (
          <p className={styles.note}>Conclua uma tarefa para ganhar o primeiro XP.</p>
        ) : (
          <ul className={styles.events}>
            {progression.recent.map((event) => (
              <li key={`${event.createdAt}-${event.reason}-${event.title ?? ''}`} className={styles.event}>
                <span className={styles.eventText}>
                  <span className={styles.eventReason}>{REASONS[event.reason]}</span>
                  <span className={styles.eventMeta}>
                    {event.title ? `${event.title} · ` : ''}
                    {formatShortDate(event.eventDate ?? todayIso(zone, new Date(event.createdAt)))}
                  </span>
                </span>
                <span className={styles.eventAmount} data-negative={event.amount < 0 || undefined}>
                  {event.amount > 0 ? `+${event.amount}` : event.amount} XP
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}

const FEMININE: ReadonlySet<RankTier> = new Set(['SILVER', 'PLATINUM', 'LEGEND'])

/** "ao Bronze", "à Prata": a frase pede o artigo certo. */
function toTier(tier: RankTier): string {
  return `${FEMININE.has(tier) ? 'à' : 'ao'} ${TIER_LABELS[tier]}`
}
