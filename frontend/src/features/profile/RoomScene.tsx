import { useMemo } from 'react'

import type { EquipmentTrack, Period } from '../../api/types'
import { dressCharacter } from '../../game/pixel/art/character'
import { windowSprite } from '../../game/pixel/art/scenery'
import { PixelScene, Placed } from '../../game/pixel/PixelSprite'
import { FLOOR_Y, ROOM_HEIGHT, ROOM_WIDTH, WINDOW_SPOT, roomLayout } from '../../game/pixel/room'
import { spriteSize } from '../../game/pixel/sprite'
import styles from './RoomScene.module.css'

type Props = {
  /** Degrau de cada trilha de melhoria. */
  equipment: Record<EquipmentTrack, number>
  /** Códigos da decoração colocada no quarto. */
  items: readonly string[]
  wearing: readonly string[]
  period: Period
  sleeping?: boolean
  label?: string
}

const WALL = '#e9dcc4'
const WALL_STRIPE = '#e2d2b4'
const BASEBOARD = '#8a5a3c'
const FLOOR = '#b9824f'
const FLOOR_LINE = '#9c6a3e'

/**
 * O quarto desenhado: parede, janela com o céu do período, piso de tábuas, as melhorias, a decoração e o
 * personagem. À noite o quarto escurece e a luminária, se houver, acende.
 */
export function RoomScene({ equipment, items, wearing, period, sleeping = false, label = 'Ilustração do seu quarto' }: Props) {
  const key = wearing.join(',')
  const player = useMemo(() => dressCharacter(key ? key.split(',') : [], { sleeping }), [key, sleeping])
  const placements = roomLayout({ equipment, items, player })
  const night = period === 'NIGHT'
  const lamp = night ? placements.find((placement) => placement.key === 'decor-lamp_desk') : undefined
  const lampHeight = lamp ? spriteSize(lamp.sprite).height : 0

  return (
    <div className={styles.frame}>
      <PixelScene width={ROOM_WIDTH} height={ROOM_HEIGHT} className={styles.scene} label={label}>
        <rect width={ROOM_WIDTH} height={FLOOR_Y} fill={WALL} />
        {Array.from({ length: ROOM_WIDTH / 8 }, (_, index) => (
          <rect key={index} x={index * 8 + 2} y={0} width={3} height={FLOOR_Y} fill={WALL_STRIPE} />
        ))}
        <rect y={FLOOR_Y - 2} width={ROOM_WIDTH} height={2} fill={BASEBOARD} />
        <rect y={FLOOR_Y} width={ROOM_WIDTH} height={ROOM_HEIGHT - FLOOR_Y} fill={FLOOR} />
        {[54, 60, 66].map((y) => (
          <rect key={y} y={y} width={ROOM_WIDTH} height={1} fill={FLOOR_LINE} />
        ))}
        {[18, 52, 90, 120, 34, 70, 106].map((x, index) => (
          <rect key={x} x={x} y={index < 4 ? FLOOR_Y : 54} width={1} height={6} fill={FLOOR_LINE} />
        ))}
        <Placed sprite={windowSprite(period)} x={WINDOW_SPOT.x} y={WINDOW_SPOT.y} />
        {placements.map((placement) => (
          <Placed
            key={placement.key}
            sprite={placement.sprite}
            x={placement.x}
            y={placement.y}
            className={placement.key === 'player' ? styles.player : undefined}
          />
        ))}
        {night && <rect width={ROOM_WIDTH} height={ROOM_HEIGHT} fill="#1a1c2c" opacity={0.32} />}
        {lamp && (
          <>
            <polygon
              points={`${lamp.x + 1},${lamp.y + 3} ${lamp.x + 7},${lamp.y + 3} ${lamp.x + 14},${lamp.y + lampHeight + 8} ${lamp.x - 6},${lamp.y + lampHeight + 8}`}
              fill="#ffcd75"
              opacity={0.22}
            />
            <Placed sprite={lamp.sprite} x={lamp.x} y={lamp.y} />
          </>
        )}
      </PixelScene>
    </div>
  )
}
