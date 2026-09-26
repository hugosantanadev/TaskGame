import { Link } from 'react-router'

import { Button } from '../components/Button'
import { PageTitle } from '../components/PageTitle'
import styles from './ErrorPages.module.css'

export function NotFoundPage() {
  return (
    <main className={styles.page}>
      <PageTitle title="Página não encontrada" />
      <h1 className={styles.title}>Página não encontrada</h1>
      <p className={styles.text}>Este endereço não existe no GasmTask.</p>
      <p>
        <Link to="/">Ir para Hoje</Link>
      </p>
    </main>
  )
}

/** Falha inesperada ao renderizar uma tela. */
export function RouteError() {
  return (
    <main className={styles.page}>
      <PageTitle title="Erro" />
      <h1 className={styles.title}>Esta tela falhou ao abrir</h1>
      <p className={styles.text}>Recarregue o app. Se continuar, volte para Hoje e tente por outro caminho.</p>
      <Button onClick={() => window.location.reload()}>Recarregar</Button>
    </main>
  )
}
