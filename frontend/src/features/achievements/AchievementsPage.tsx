import { asApiError } from '../../api/errors'
import type { Achievement } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { ButtonLink } from '../../components/Button'
import { LockIcon, TrophyIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { safeTimeZone } from '../../lib/datetime'
import { formatShortDate, todayIso } from '../../lib/days'
import styles from '../profile/Collection.module.css'
import { useAchievements } from './achievementsApi'

export function AchievementsPage() {
  const achievements = useAchievements()
  const unlocked = achievements.data?.filter((achievement) => achievement.unlocked).length ?? 0

  return (
    <div className={styles.page}>
      <PageTitle title="Conquistas" />
      <ButtonLink to="/perfil" variant="quiet" className={styles.back}>
        Voltar ao perfil
      </ButtonLink>
      <h1 className={styles.heading}>Conquistas</h1>
      {achievements.data && (
        <p className={styles.note}>
          {unlocked} de {achievements.data.length} desbloqueadas. Elas são avaliadas a cada conclusão e na virada do dia.
        </p>
      )}
      {achievements.isPending && <p className={styles.note}>Carregando…</p>}
      {achievements.isError && (
        <p className={styles.error} role="alert">
          {asApiError(achievements.error).message}
        </p>
      )}
      <ul className={styles.list}>
        {achievements.data?.map((achievement) => (
          <li key={achievement.code}>
            <AchievementCard achievement={achievement} />
          </li>
        ))}
      </ul>
    </div>
  )
}

function AchievementCard({ achievement }: { achievement: Achievement }) {
  const zone = safeTimeZone(useCurrentUser().timeZone)
  return (
    <article className={styles.row} data-unlocked={achievement.unlocked || undefined}>
      <span className={styles.badge} aria-hidden="true">
        {achievement.unlocked ? <TrophyIcon /> : <LockIcon />}
      </span>
      <div className={styles.rowText}>
        <h2 className={styles.rowTitle}>{achievement.name}</h2>
        <p className={styles.rowMeta}>{achievement.description}</p>
        {achievement.unlocked && achievement.unlockedAt ? (
          <p className={styles.rowMeta}>Desbloqueada em {formatShortDate(todayIso(zone, new Date(achievement.unlockedAt)))}</p>
        ) : (
          <>
            <div
              className={styles.bar}
              role="progressbar"
              aria-label={`Progresso de ${achievement.name}`}
              aria-valuemin={0}
              aria-valuemax={achievement.threshold}
              aria-valuenow={achievement.progress}
            >
              <span style={{ width: `${(achievement.progress / achievement.threshold) * 100}%` }} />
            </div>
            <p className={styles.rowMeta}>
              {achievement.progress} de {achievement.threshold}
            </p>
          </>
        )}
      </div>
    </article>
  )
}
