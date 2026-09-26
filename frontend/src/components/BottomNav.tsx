import type { ComponentType, SVGProps } from 'react'
import { NavLink } from 'react-router'

import styles from './BottomNav.module.css'
import { ProfileIcon, RankingIcon, StatsIcon, StoreIcon, TodayIcon } from './icons'

type NavItem = { to: string; label: string; icon: ComponentType<SVGProps<SVGSVGElement>>; end?: boolean }

const ITEMS: NavItem[] = [
  { to: '/', label: 'Hoje', icon: TodayIcon, end: true },
  { to: '/estatisticas', label: 'Estatísticas', icon: StatsIcon },
  { to: '/loja', label: 'Loja', icon: StoreIcon },
  { to: '/ranking', label: 'Ranking', icon: RankingIcon },
  { to: '/perfil', label: 'Perfil', icon: ProfileIcon },
]

export function BottomNav() {
  return (
    <nav className={styles.nav} aria-label="Navegação principal">
      <ul className={styles.list}>
        {ITEMS.map(({ to, label, icon: Icon, end }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={end}
              className={({ isActive }) => (isActive ? `${styles.link} ${styles.active}` : styles.link)}
            >
              <Icon className={styles.icon} />
              <span className={styles.label}>{label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
