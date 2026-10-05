import type { CSSProperties } from 'react'

import type { StoreItem, StoreItemCategory } from '../../api/types'
import { CapIcon, GlassesIcon, PlantIcon, ShirtIcon, SofaIcon } from '../../components/gameIcons'
import { itemSprite } from '../../game/pixel/items'
import { PixelSprite } from '../../game/pixel/PixelSprite'
import styles from './ItemSticker.module.css'

const COLORS: Record<StoreItemCategory, string> = {
  FURNITURE: 'var(--cat-home)',
  DECORATION: 'var(--cat-reading)',
  CHARACTER: 'var(--cat-spirituality)',
  EQUIPMENT: 'var(--cat-study)',
}

/** Ícone genérico para itens que ainda não ganharam desenho (itens novos no catálogo). */
function FallbackIcon({ item }: { item: StoreItem }) {
  if (item.category === 'FURNITURE') return <SofaIcon />
  if (item.category === 'DECORATION') return <PlantIcon />
  if (item.slot === 'HEAD') return <CapIcon />
  return item.slot === 'OUTFIT' ? <ShirtIcon /> : <GlassesIcon />
}

/** Vitrine do item: o desenho em pixel art numa moldura com a cor da categoria. */
export function ItemSticker({ item, size = 'm' }: { item: StoreItem; size?: 'm' | 'l' }) {
  const sprite = itemSprite(item.code)
  return (
    <span className={styles.sticker} data-size={size} style={{ '--sticker': COLORS[item.category] } as CSSProperties} aria-hidden="true">
      {sprite ? <PixelSprite sprite={sprite} className={styles.art} /> : <FallbackIcon item={item} />}
    </span>
  )
}
