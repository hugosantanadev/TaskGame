import type { CSSProperties } from 'react'

import type { Occurrence, OccurrenceStatus } from '../../api/types'
import { CheckIcon, CrossIcon } from '../../components/gameIcons'
import { categoryColor, categoryLabel } from '../../lib/categories'
import { formatTime } from '../../lib/days'
import styles from './OccurrenceRow.module.css'

const STATUS_LABEL: Record<OccurrenceStatus, string> = {
  PENDING: 'pendente',
  COMPLETED: 'concluída',
  MISSED: 'perdida',
}

type OccurrenceRowProps = { occurrence: Occurrence; next?: boolean; onSelect: (occurrence: Occurrence) => void }

/** Uma linha da agenda: horário na margem, fita da categoria e o estado no círculo à direita. */
export function OccurrenceRow({ occurrence, next = false, onSelect }: OccurrenceRowProps) {
  const time = formatTime(occurrence.plannedTime)
  return (
    <li
      className={styles.row}
      data-status={occurrence.status}
      data-next={next || undefined}
      style={{ '--cat': categoryColor(occurrence.category) } as CSSProperties}
    >
      <span className={styles.time}>{time ?? ''}</span>
      <button type="button" className={styles.card} onClick={() => onSelect(occurrence)} aria-haspopup="dialog">
        <span className={styles.text}>
          <span className={styles.title}>{occurrence.title}</span>
          <span className={styles.meta}>
            <span className={styles.category}>{categoryLabel(occurrence.category)}</span>
            {occurrence.kind === 'EXTRA' && <span className={styles.tag}>extra</span>}
            {occurrence.requiresProof && <span className={styles.tag}>pede foto</span>}
            {next && <span className={styles.nextTag}>próxima</span>}
          </span>
        </span>
        <span className={styles.mark} aria-hidden="true">
          {occurrence.status === 'COMPLETED' && <CheckIcon />}
          {occurrence.status === 'MISSED' && <CrossIcon />}
        </span>
        <span className="visually-hidden">, {STATUS_LABEL[occurrence.status]}</span>
      </button>
    </li>
  )
}
