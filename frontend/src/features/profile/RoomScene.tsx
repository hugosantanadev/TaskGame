import { useMemo } from 'react'

import type { Period } from '../../api/types'
import { CHARACTER_HEIGHT, dressCharacter } from '../../game/pixel/art/character'
import { FURNITURE } from '../../game/pixel/art/furniture'
import { windowSprite } from '../../game/pixel/art/scenery'
import { PixelScene, Placed } from '../../game/pixel/PixelSprite'
import { roomLayout } from '../../game/pixel/room'
import styles from './RoomScene.module.css'

/* O quarto em 96 × 64 pixels de arte: parede até a linha 44, piso embaixo. */
const WIDTH = 96
const HEIGHT = 64
const FLOOR = 44

type Props = { items: readonly string[]; wearing: readonly string[]; period: Period; sleeping?: boolean }

/** O quarto desenhado: parede, janela com o céu do período, piso de tábuas, os itens e o personagem. */
export function RoomScene({ items, wearing, period, sleeping = false }: Props) {
  const layout = roomLayout(items)
  const key = wearing.join(',')
  const player = useMemo(() => dressCharacter(key ? key.split(',') : [], { sleeping }), [key, sleeping])
  return (
    <div className={styles.frame}>
      <PixelScene width={WIDTH} height={HEIGHT} className={styles.scene} label="Ilustração do seu quarto">
        <rect width={WIDTH} height={FLOOR} fill="#f4e4c1" />
        {Array.from({ length: WIDTH / 8 }, (_, index) => (
          <rect key={index} x={index * 8 + 2} y={0} width={3} height={FLOOR} fill="#efd8a6" />
        ))}
        <rect y={FLOOR - 2} width={WIDTH} height={2} fill="#a0612e" />
        <rect y={FLOOR} width={WIDTH} height={HEIGHT - FLOOR} fill="#c98a4b" />
        {[49, 54, 59].map((y) => (
          <rect key={y} y={y} width={WIDTH} height={1} fill="#a86b36" />
        ))}
        {[14, 40, 66, 27, 53, 79].map((x, index) => (
          <rect key={x} x={x} y={index < 3 ? FLOOR : 49} width={1} height={5} fill="#a86b36" />
        ))}
        <Placed sprite={windowSprite(period)} x={38} y={5} />
        {layout.map(({ code, spot }) => (
          <Placed key={code} sprite={FURNITURE[code]} x={spot.x} y={spot.y} />
        ))}
        <Placed sprite={player} x={40} y={56 - CHARACTER_HEIGHT} className={styles.player} />
      </PixelScene>
    </div>
  )
}
