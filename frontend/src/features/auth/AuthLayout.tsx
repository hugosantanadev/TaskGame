import type { ReactNode } from 'react'
import { Link } from 'react-router'

import { PageTitle } from '../../components/PageTitle'
import { Wordmark } from '../../components/Wordmark'
import { Avatar } from '../../game/pixel/Avatar'
import styles from './AuthLayout.module.css'

type AuthLayoutProps = { title: string; footer: ReactNode; children: ReactNode }

export function AuthLayout({ title, footer, children }: AuthLayoutProps) {
  return (
    <main className={styles.page}>
      <PageTitle title={title} />
      <header className={styles.brand}>
        <Link to="/bem-vindo" className={styles.home} aria-label="GasmTask: conhecer o app">
          <Wordmark size="large" />
        </Link>
        <p className={styles.tagline}>Suas tarefas de verdade valem XP, moedas e dias seguidos.</p>
      </header>
      <section className={styles.sheet} aria-labelledby="auth-title">
        <Avatar wearing={['cap_red']} scale={3} className={styles.avatar} />
        <h1 id="auth-title" className={styles.title}>
          {title}
        </h1>
        {children}
      </section>
      <p className={styles.footer}>{footer}</p>
    </main>
  )
}
