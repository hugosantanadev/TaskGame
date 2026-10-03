import { useEffect, useState } from 'react'

import type { DailyChallenge } from '../api/types'
import { onReward, type RewardEvent } from '../game/rewardFeedback'
import { listJoin, plural } from '../lib/days'
import { ATTRIBUTE_LABELS } from '../lib/attributes'
import { challengeName } from '../lib/challenges'
import { compareRanks, rankLabel } from '../lib/rank'
import { CoinIcon, CrossIcon, FlameIcon, ShirtIcon, StarIcon, TrophyIcon } from './gameIcons'
import { RankBadge } from './RankBadge'
import styles from './RewardToast.module.css'

/** Aviso de recompensa: mostra o que a conclusão rendeu, parte por parte, e se a sequência subiu. */
export function RewardToast() {
  const [event, setEvent] = useState<RewardEvent | null>(null)

  useEffect(() => onReward(setEvent), [])

  useEffect(() => {
    if (!event) return
    const timer = window.setTimeout(() => setEvent(null), 6500)
    return () => window.clearTimeout(timer)
  }, [event])

  return (
    <div className={styles.region} role="status" aria-live="polite">
      {event && (
        <div className={styles.toast}>
          <div className={styles.text}>
            {event.kind === 'completed' && <Completed event={event} />}
            {event.kind === 'proof' && <ProofSent event={event} />}
            {event.kind === 'rank' && <RankChanged event={event} />}
            {event.kind === 'purchase' && (
              <>
                <p className={styles.title}>Comprado: {event.title}</p>
                <p className={styles.detail}>Já está na sua coleção. Saldo: {plural(event.balance, 'moeda', 'moedas')}.</p>
              </>
            )}
          </div>
          <button type="button" className={styles.close} onClick={() => setEvent(null)} aria-label="Fechar aviso">
            <CrossIcon />
          </button>
        </div>
      )}
    </div>
  )
}

function Completed({ event }: { event: Extract<RewardEvent, { kind: 'completed' }> }) {
  const { reward, streak, xp, attribute } = event.result
  const attributeName = ATTRIBUTE_LABELS[attribute.status.attribute]
  const parts = [`${reward.baseCoins} da tarefa`]
  if (reward.onTimeBonus > 0) parts.push(`${reward.onTimeBonus} pelo horário`)
  if (reward.proofBonus > 0) parts.push(`${reward.proofBonus} pela foto`)
  return (
    <>
      <p className={styles.title}>Feito: {event.title}</p>
      <p className={styles.amounts}>
        <span className={styles.points}>
          <StarIcon /> +{plural(reward.points, 'ponto', 'pontos')}
        </span>
        <span className={styles.coins}>
          <CoinIcon /> +{plural(reward.totalCoins, 'moeda', 'moedas')}
        </span>
        {xp.gained > 0 && <span className={styles.xp}>+{xp.gained} XP</span>}
        {attribute.gained > 0 && (
          <span className={styles.xp}>
            {attributeName} +{attribute.gained}
          </span>
        )}
      </p>
      {attribute.leveledUp && (
        <p className={styles.rankUp}>
          {attributeName} subiu para o nível {attribute.status.level}!
        </p>
      )}
      {parts.length > 1 && <p className={styles.detail}>Moedas: {listJoin(parts)}.</p>}
      {xp.promoted && (
        <p className={styles.rankUp}>
          Subiu para <RankBadge rank={xp.status} size="s" />
        </p>
      )}
      {xp.unlockedItems.map((item) => (
        <p key={item.code} className={styles.achievement}>
          <ShirtIcon /> Roupa de elo: {item.name}
        </p>
      ))}
      {streak.increasedNow && (
        <p className={styles.streak}>
          <FlameIcon /> Dia cumprido. Sequência de {plural(streak.current, 'dia', 'dias')}.
        </p>
      )}
      <ChallengeLines challenges={event.result.completedChallenges} />
      {event.result.unlockedAchievements.map((achievement) => (
        <p key={achievement.code} className={styles.achievement}>
          <TrophyIcon /> Conquista: {achievement.name}
        </p>
      ))}
    </>
  )
}

function ChallengeLines({ challenges }: { challenges: DailyChallenge[] }) {
  return challenges.map((challenge) => (
    <p key={challenge.code} className={styles.achievement}>
      <StarIcon /> Desafio cumprido: {challengeName(challenge.code)} (+{challenge.xpReward} XP
      {challenge.coinReward > 0 ? `, +${plural(challenge.coinReward, 'moeda', 'moedas')}` : ''})
    </p>
  ))
}

function RankChanged({ event }: { event: Extract<RewardEvent, { kind: 'rank' }> }) {
  const up = compareRanks(event.to, event.from) > 0
  return (
    <>
      <p className={styles.title}>{up ? 'Você subiu de elo!' : `Você caiu para ${rankLabel(event.to)}`}</p>
      <p className={styles.rankUp}>
        <RankBadge rank={event.to} size="s" />
      </p>
      {!up && <p className={styles.detail}>Obrigatórias perdidas custam XP. Cumpra o dia de hoje para voltar a subir.</p>}
    </>
  )
}

function ProofSent({ event }: { event: Extract<RewardEvent, { kind: 'proof' }> }) {
  return (
    <>
      <p className={styles.title}>Foto enviada: {event.title}</p>
      {event.result.proofBonus > 0 && (
        <p className={styles.amounts}>
          <span className={styles.coins}>
            <CoinIcon /> +{plural(event.result.proofBonus, 'moeda', 'moedas')}
          </span>
        </p>
      )}
      <ChallengeLines challenges={event.result.completedChallenges} />
    </>
  )
}
