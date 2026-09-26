import type { SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement>

/** Ícones de traço, como desenhados à caneta. Decorativos: o texto ao lado dá o nome. */
function Icon({ children, ...props }: IconProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      {children}
    </svg>
  )
}

export function TodayIcon(props: IconProps) {
  return (
    <Icon {...props}>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2" />
      <path d="M3.5 9.5h17M8 3v4M16 3v4" />
      <path d="m9 14.75 2 2 4-4" />
    </Icon>
  )
}

export function StatsIcon(props: IconProps) {
  return (
    <Icon {...props}>
      <path d="M4 20h16" />
      <path d="M7 20v-6M12 20V7M17 20v-9" />
    </Icon>
  )
}

export function StoreIcon(props: IconProps) {
  return (
    <Icon {...props}>
      <path d="M5 8.5h14l-1.1 11a1.5 1.5 0 0 1-1.5 1.5H7.6a1.5 1.5 0 0 1-1.5-1.5L5 8.5Z" />
      <path d="M9 8.5V7a3 3 0 0 1 6 0v1.5" />
    </Icon>
  )
}

export function RankingIcon(props: IconProps) {
  return (
    <Icon {...props}>
      <path d="M3 20.5h18" />
      <path d="M9 20.5V9h6v11.5" />
      <path d="M4 20.5V14h5M15 16h5v4.5" />
    </Icon>
  )
}

export function ProfileIcon(props: IconProps) {
  return (
    <Icon {...props}>
      <circle cx="12" cy="8.5" r="3.75" />
      <path d="M4.75 20.5c.9-3.7 3.7-5.75 7.25-5.75s6.35 2.05 7.25 5.75" />
    </Icon>
  )
}
