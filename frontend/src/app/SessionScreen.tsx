import { useAuth } from '../auth/context'
import { Button } from '../components/Button'
import { Wordmark } from '../components/Wordmark'
import styles from './SessionScreen.module.css'

/** Enquanto a sessão é restaurada, ou quando o servidor não respondeu. */
export function SessionScreen() {
  const { state, retry } = useAuth()
  return (
    <main className={styles.screen}>
      <Wordmark size="large" />
      {state.status === 'offline' ? (
        <div className={styles.offline} role="alert">
          <p className={styles.title}>Não foi possível falar com o servidor.</p>
          <p className={styles.hint}>Confira sua conexão. Se ela estiver normal, o servidor pode estar fora do ar.</p>
          <Button onClick={retry}>Tentar de novo</Button>
        </div>
      ) : (
        <p className={styles.loading} aria-live="polite">
          <span className={styles.dots} aria-hidden="true">
            <span />
            <span />
            <span />
          </span>
          Abrindo sua agenda…
        </p>
      )}
    </main>
  )
}
