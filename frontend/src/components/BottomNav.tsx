import type { ComponentType, SVGProps } from 'react'
import { Link, useLocation } from 'react-router'

import styles from './BottomNav.module.css'
import { ProfileIcon, RankingIcon, StatsIcon, StoreIcon, TodayIcon } from './icons'

/** `section`: outras rotas que pertencem à mesma aba (Semana, missões e extras ficam sob "Hoje"). */
type NavItem = { to: string; label: string; icon: ComponentType<SVGProps<SVGSVGElement>>; section?: string[] }

const ITEMS: NavItem[] = [
  { to: '/', label: 'Hoje', icon: TodayIcon, section: ['/semana', '/missoes', '/extras'] },
  { to: '/estatisticas', label: 'Estatísticas', icon: StatsIcon },
  { to: '/loja', label: 'Loja', icon: StoreIcon },
  { to: '/ranking', label: 'Ranking', icon: RankingIcon },
  { to: '/perfil', label: 'Perfil', icon: ProfileIcon },
]

function isActive(item: NavItem, pathname: string): boolean {
  if (item.to === '/') return pathname === '/' || (item.section ?? []).some((prefix) => pathname.startsWith(prefix))
  return pathname.startsWith(item.to)
}

export function BottomNav() {
  const { pathname } = useLocation()
  return (
    <nav className={styles.nav} aria-label="Navegação principal">
      <ul className={styles.list}>
        {ITEMS.map((item) => {
          const active = isActive(item, pathname)
          const Icon = item.icon
          return (
            <li key={item.to}>
              <Link
                to={item.to}
                className={active ? `${styles.link} ${styles.active ?? ''}` : styles.link}
                aria-current={active ? 'page' : undefined}
              >
                <Icon className={styles.icon} aria-hidden="true" />
                <span className={styles.label}>{item.label}</span>
              </Link>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
