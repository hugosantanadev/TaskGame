import { useEffect, useState } from 'react'

import { onReward, type RewardEvent } from '../game/rewardFeedback'
import { listJoin, plural } from '../lib/days'
import { CoinIcon, CrossIcon, FlameIcon, StarIcon, TrophyIcon } from './gameIcons'
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
  const { reward, streak } = event.result
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
      </p>
      {parts.length > 1 && <p className={styles.detail}>Moedas: {listJoin(parts)}.</p>}
      {streak.increasedNow && (
        <p className={styles.streak}>
          <FlameIcon /> Dia cumprido. Sequência de {plural(streak.current, 'dia', 'dias')}.
        </p>
      )}
      {event.result.unlockedAchievements.map((achievement) => (
        <p key={achievement.code} className={styles.achievement}>
          <TrophyIcon /> Conquista: {achievement.name}
        </p>
      ))}
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
    </>
  )
}
