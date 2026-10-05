import { Link } from 'react-router'

import type { CharacterState, Period, TitleCode } from '../../api/types'
import { PixelIcon } from '../../game/pixel/PixelSprite'
import { tiersOf } from '../../lib/equipment'
import { isMasterTitle, titleLabel } from '../../lib/titles'
import { RoomScene } from '../profile/RoomScene'
import { useEquipment, useRoom } from '../store/storeApi'
import styles from './RoomHero.module.css'

type Props = {
  period: Period
  greeting: string
  /** Data escrita por extenso para leitores de tela e curta na tela. */
  dateLabel: string
  dateShort: string
  wearing: readonly string[]
  state: CharacterState
  title: TitleCode | null
}

/**
 * O topo da tela Hoje: o quarto do personagem, que é o retrato do progresso. Por cima, só o cumprimento, a data,
 * o título e um atalho para melhorar o quarto.
 */
export function RoomHero({ period, greeting, dateLabel, dateShort, wearing, state, title }: Props) {
  const room = useRoom()
  const equipment = useEquipment()

  return (
    <header className={styles.hero}>
      <div className={styles.bar}>
        <div className={styles.heading}>
          <p className={styles.greeting}>{greeting}</p>
          <h1 className={styles.date}>
            <span className="visually-hidden">{dateLabel}</span>
            <span aria-hidden="true">{dateShort}</span>
            {title && (
              <span className={styles.title} data-master={isMasterTitle(title) || undefined}>
                <PixelIcon name="star" />
                {titleLabel(title)}
              </span>
            )}
          </h1>
        </div>
        <Link to="/loja" className={styles.upgrade}>
          Melhorar
        </Link>
      </div>
      {room.isPending || equipment.isPending ? (
        <div className={styles.placeholder} aria-hidden="true" />
      ) : (
      <RoomScene
        equipment={tiersOf(equipment.data)}
        items={room.data?.items.map((placed) => placed.item.code) ?? []}
        wearing={wearing}
        sleeping={state === 'SLEEPING'}
        period={period}
      />
      )}
    </header>
  )
}
