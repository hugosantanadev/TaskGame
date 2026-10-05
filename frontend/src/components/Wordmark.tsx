import { MASCOT } from '../game/pixel/art/brand'
import { PixelSprite } from '../game/pixel/PixelSprite'
import styles from './Wordmark.module.css'

/** Marca: o rosto do mascote numa plaquinha amarela, ao lado do nome em letra de pixel. */
export function Wordmark({ size = 'medium' }: { size?: 'medium' | 'large' }) {
  return (
    <span className={`${styles.wordmark} ${styles[size]}`}>
      <span className={styles.mark} aria-hidden="true">
        <PixelSprite sprite={MASCOT} className={styles.mascot} />
      </span>
      GasmTask
    </span>
  )
}
