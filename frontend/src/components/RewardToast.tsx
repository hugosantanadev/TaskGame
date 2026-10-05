import { useEffect, useState } from 'react'

import type { DailyChallenge } from '../api/types'
import { onReward, type RewardEvent } from '../game/rewardFeedback'
import { listJoin, plural } from '../lib/days'
import { ATTRIBUTE_LABELS } from '../lib/attributes'
import { challengeName } from '../lib/challenges'
import { compareRanks, rankLabel } from '../lib/rank'
import { PixelIcon } from '../game/pixel/PixelSprite'
import { CrossIcon, TrophyIcon } from './gameIcons'
import { RankBadge } from './RankBadge'
import styles from './RewardToast.module.css'

/** Aviso de recompensa: mostra o que a conclusão rendeu, parte por parte, e se a sequência subiu. */
export function RewardToast() {
  const [event, setEvent] = useState<RewardEvent | null>(null)

  // Baú aberto e subida de elo viram a comemoração em tela cheia (Celebration); aqui ficam os avisos pequenos
  useEffect(
    () =>
      onReward((next) => {
        if (next.kind === 'chest') return
        if (next.kind === 'rank' && compareRanks(next.to, next.from) > 0) return
        setEvent(next)
      }),
    [],
  )

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
                <p className={styles.detail}>{event.detail ?? 'Já está na sua coleção.'} Saldo: {plural(event.balance, 'moeda', 'moedas')}.</p>
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
          <PixelIcon name="star" /> +{plural(reward.points, 'ponto', 'pontos')}
        </span>
        <span className={styles.coins}>
          <PixelIcon name="coin" /> +{plural(reward.totalCoins, 'moeda', 'moedas')}
        </span>
        {xp.gained > 0 && (
          <span className={styles.xp}>
            <PixelIcon name="gem" /> +{xp.gained} XP
          </span>
        )}
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
      {streak.increasedNow && (
        <p className={styles.streak}>
          <PixelIcon name="flame" /> Dia cumprido. Sequência de {plural(streak.current, 'dia', 'dias')}.
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
      <PixelIcon name="star" /> Desafio cumprido: {challengeName(challenge.code)} (+{challenge.xpReward} XP
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
            <PixelIcon name="coin" /> +{plural(event.result.proofBonus, 'moeda', 'moedas')}
          </span>
        </p>
      )}
      <ChallengeLines challenges={event.result.completedChallenges} />
    </>
  )
}
