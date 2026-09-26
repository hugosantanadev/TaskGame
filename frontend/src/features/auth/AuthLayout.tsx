import type { ReactNode } from 'react'

import { PageTitle } from '../../components/PageTitle'
import { Wordmark } from '../../components/Wordmark'
import styles from './AuthLayout.module.css'

type AuthLayoutProps = { title: string; footer: ReactNode; children: ReactNode }

export function AuthLayout({ title, footer, children }: AuthLayoutProps) {
  return (
    <main className={styles.page}>
      <PageTitle title={title} />
      <header className={styles.brand}>
        <Wordmark size="large" />
        <p className={styles.tagline}>Suas tarefas de verdade valem pontos, moedas e dias seguidos.</p>
      </header>
      <section className={styles.sheet} aria-labelledby="auth-title">
        <h1 id="auth-title" className={styles.title}>
          {title}
        </h1>
        {children}
      </section>
      <p className={styles.footer}>{footer}</p>
    </main>
  )
}
