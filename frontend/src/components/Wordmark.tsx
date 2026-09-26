import styles from './Wordmark.module.css'

/** Marca: um visto de caneta sobre um traço de marca-texto. */
export function Wordmark({ size = 'medium' }: { size?: 'medium' | 'large' }) {
  return (
    <span className={`${styles.wordmark} ${styles[size]}`}>
      <svg viewBox="0 0 32 32" className={styles.mark} aria-hidden="true" focusable="false">
        <rect className={styles.highlight} x="2" y="15" width="28" height="11" rx="2" transform="rotate(-8 16 20.5)" />
        <path className={styles.check} d="M7 16.5l5.5 5.5L25.5 8" />
      </svg>
      GasmTask
    </span>
  )
}
