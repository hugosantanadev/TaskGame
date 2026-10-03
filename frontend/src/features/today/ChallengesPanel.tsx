import type { DailyChallenge } from '../../api/types'
import { CheckIcon } from '../../components/gameIcons'
import { challengeGoal, challengeName } from '../../lib/challenges'
import { plural } from '../../lib/days'
import styles from './ChallengesPanel.module.css'

/** Desafios do dia: três metas que mudam todo dia, valendo XP e moedas. */
export function ChallengesPanel({ challenges }: { challenges: DailyChallenge[] }) {
  if (challenges.length === 0) return null
  const done = challenges.filter((challenge) => challenge.completed).length

  return (
    <section className={styles.panel} aria-labelledby="challenges-title">
      <div className={styles.head}>
        <h2 id="challenges-title" className={styles.title}>
          Desafios do dia
        </h2>
        <span className={styles.count}>
          {done} de {challenges.length}
        </span>
      </div>
      <ul className={styles.list}>
        {challenges.map((challenge) => (
          <li key={challenge.code} className={styles.challenge} data-done={challenge.completed || undefined}>
            <span className={styles.mark} aria-hidden="true">
              {challenge.completed ? <CheckIcon /> : `${challenge.progress}/${challenge.target}`}
            </span>
            <span className={styles.text}>
              <span className={styles.name}>
                {challengeName(challenge.code)}
                <span className="visually-hidden">
                  {challenge.completed ? ', cumprido' : `, ${challenge.progress} de ${challenge.target}`}
                </span>
              </span>
              <span className={styles.goal}>{challengeGoal(challenge)}</span>
            </span>
            <span className={styles.reward}>
              +{challenge.xpReward} XP
              {challenge.coinReward > 0 && <span> · +{plural(challenge.coinReward, 'moeda', 'moedas')}</span>}
            </span>
          </li>
        ))}
      </ul>
    </section>
  )
}
