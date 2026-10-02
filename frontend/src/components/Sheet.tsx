import { useEffect, useId, useRef, type ReactNode } from 'react'

import { CrossIcon } from './gameIcons'
import styles from './Sheet.module.css'

type SheetProps = { open: boolean; title: string; onClose: () => void; children: ReactNode }

/**
 * Painel que sobe da parte de baixo da tela. Usa o <dialog> nativo: o foco fica preso dentro,
 * Esc fecha e o leitor de tela anuncia o título. Tocar fora do painel também fecha.
 */
export function Sheet({ open, title, onClose, children }: SheetProps) {
  const ref = useRef<HTMLDialogElement>(null)
  const titleId = useId()

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      className={styles.sheet}
      aria-labelledby={titleId}
      onClose={onClose}
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      {open && (
        <div className={styles.body}>
          <div className={styles.head}>
            <h2 id={titleId} className={styles.title}>
              {title}
            </h2>
            <button type="button" className={styles.close} onClick={onClose} aria-label="Fechar">
              <CrossIcon />
            </button>
          </div>
          {children}
        </div>
      )}
    </dialog>
  )
}
