import { useRegisterSW } from 'virtual:pwa-register/react'

import styles from './UpdatePrompt.module.css'

const ONE_HOUR = 60 * 60 * 1000

/** Aviso de nova versão do app instalado. A troca só acontece quando a pessoa aceita. */
export function UpdatePrompt() {
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegisteredSW(_swUrl, registration) {
      if (registration) {
        window.setInterval(() => void registration.update(), ONE_HOUR)
      }
    },
  })

  if (!needRefresh) return null

  return (
    <div className={styles.banner} role="status">
      <span className={styles.text}>Nova versão do GasmTask disponível.</span>
      <button type="button" className={styles.update} onClick={() => void updateServiceWorker(true)}>
        Atualizar
      </button>
      <button type="button" className={styles.dismiss} onClick={() => setNeedRefresh(false)}>
        Agora não
      </button>
    </div>
  )
}
