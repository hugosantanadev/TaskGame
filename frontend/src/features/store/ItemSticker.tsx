import type { CSSProperties } from 'react'

import type { StoreItem, StoreItemCategory } from '../../api/types'
import { CapIcon, GlassesIcon, PlantIcon, ShirtIcon, SofaIcon } from '../../components/gameIcons'
import styles from './ItemSticker.module.css'

const COLORS: Record<StoreItemCategory, string> = {
  FURNITURE: 'var(--cat-home)',
  DECORATION: 'var(--cat-reading)',
  CHARACTER: 'var(--cat-spirituality)',
}

/**
 * Adesivo do item: cor pela categoria e ícone pelo tipo. A camada visual futura troca isto pelo sprite
 * da `assetKey`, sem mudar quem usa o componente.
 */
export function ItemSticker({ item, size = 'm' }: { item: StoreItem; size?: 'm' | 'l' }) {
  const Icon =
    item.category === 'FURNITURE'
      ? SofaIcon
      : item.category === 'DECORATION'
        ? PlantIcon
        : item.slot === 'HEAD'
          ? CapIcon
          : item.slot === 'OUTFIT'
            ? ShirtIcon
            : GlassesIcon
  return (
    <span className={styles.sticker} data-size={size} style={{ '--sticker': COLORS[item.category] } as CSSProperties} aria-hidden="true">
      <Icon />
    </span>
  )
}
