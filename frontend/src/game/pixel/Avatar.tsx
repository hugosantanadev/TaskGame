import { useMemo } from 'react'

import { dressCharacter } from './art/character'
import { PixelSprite } from './PixelSprite'

type Props = {
  /** Códigos dos itens vestidos (store item `code`). */
  wearing: readonly string[]
  sleeping?: boolean
  scale?: number
  className?: string
  label?: string
}

/** O personagem do jogador, com as roupas que veste. */
export function Avatar({ wearing, sleeping = false, scale = 4, className, label }: Props) {
  const key = wearing.join(',')
  const sprite = useMemo(() => dressCharacter(key ? key.split(',') : [], { sleeping }), [key, sleeping])
  return <PixelSprite sprite={sprite} scale={scale} className={className} label={label} />
}
