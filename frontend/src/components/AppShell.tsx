import { Outlet } from 'react-router'

import { ReminderNotifier } from '../features/reminders/ReminderNotifier'
import styles from './AppShell.module.css'
import { BottomNav } from './BottomNav'
import { Celebration } from './Celebration'
import { RewardToast } from './RewardToast'

/** Moldura das telas logadas: conteúdo em coluna única, barra de navegação inferior e os avisos. */
export function AppShell() {
  return (
    <>
      <RewardToast />
      <Celebration />
      <ReminderNotifier />
      <main className={styles.content}>
        <Outlet />
      </main>
      <BottomNav />
    </>
  )
}
