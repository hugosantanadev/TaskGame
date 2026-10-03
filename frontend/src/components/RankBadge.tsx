import type { CSSProperties } from 'react'

import type { RankRef } from '../api/types'
import { rankLabel, tierColor } from '../lib/rank'
import styles from './RankBadge.module.css'

type Props = {
  rank: RankRef
  size?: 's' | 'm' | 'l'
  /** Mostra o nome ao lado do emblema; sem ele, o nome fica só para leitores de tela. */
  showLabel?: boolean
}

/** Emblema do elo: escudo na cor do elo com as divisões como marcas, e o nome ("Prata 2") ao lado. */
export function RankBadge({ rank, size = 'm', showLabel = true }: Props) {
  const label = rankLabel(rank)
  const pips = rank.division ?? 0
  return (
    <span className={styles.badge} data-size={size} style={{ '--tier': tierColor(rank.tier) } as CSSProperties}>
      <span className={styles.shield} data-tier={rank.tier} aria-hidden="true">
        {rank.division === null ? (
          <span className={styles.star}>★</span>
        ) : (
          <span className={styles.pips}>
            {Array.from({ length: 3 }, (_, index) => (
              <span key={index} className={styles.pip} data-on={index < pips || undefined} />
            ))}
          </span>
        )}
      </span>
      {showLabel ? <span className={styles.label}>{label}</span> : <span className="visually-hidden">{label}</span>}
    </span>
  )
}
