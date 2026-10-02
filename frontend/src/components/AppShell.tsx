import { Outlet } from 'react-router'

import styles from './AppShell.module.css'
import { BottomNav } from './BottomNav'
import { RewardToast } from './RewardToast'

/** Moldura das telas logadas: conteúdo em coluna única e a barra de navegação inferior. */
export function AppShell() {
  return (
    <>
      <RewardToast />
      <main className={styles.content}>
        <Outlet />
      </main>
      <BottomNav />
    </>
  )
}
