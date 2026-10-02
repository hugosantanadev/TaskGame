import { NavLink } from 'react-router'

import styles from './DayTabs.module.css'

/** Alterna entre o dia de hoje e o plano da semana. */
export function DayTabs() {
  const tabClass = ({ isActive }: { isActive: boolean }) => (isActive ? `${styles.tab} ${styles.active}` : styles.tab)
  return (
    <nav className={styles.tabs} aria-label="Visão do plano">
      <NavLink to="/" end className={tabClass}>
        Hoje
      </NavLink>
      <NavLink to="/semana" className={tabClass}>
        Semana
      </NavLink>
    </nav>
  )
}
