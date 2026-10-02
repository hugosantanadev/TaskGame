import type { SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement>

function Svg(props: IconProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      width="1em"
      height="1em"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      {...props}
    />
  )
}

export function FlameIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M12 3c.6 3.6 4.8 5.2 4.8 9.6a4.8 4.8 0 0 1-9.6 0c0-2.1 1-3.4 2.1-4.2.2 1.7 1 2.7 2.1 3.1 0-3.1.6-5.6.6-8.5z" />
    </Svg>
  )
}

export function CoinIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <circle cx="12" cy="12" r="8.5" />
      <circle cx="12" cy="12" r="4.5" />
    </Svg>
  )
}

export function StarIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="m12 3.5 2.6 5.3 5.8.8-4.2 4.1 1 5.8-5.2-2.7-5.2 2.7 1-5.8-4.2-4.1 5.8-.8z" />
    </Svg>
  )
}

export function CheckIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="m5 12.5 4.5 4.5L19 7.5" />
    </Svg>
  )
}

export function CrossIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M6.5 6.5l11 11M17.5 6.5l-11 11" />
    </Svg>
  )
}

export function CameraIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M4 8.5h3.2L8.7 6h6.6l1.5 2.5H20v10H4z" />
      <circle cx="12" cy="13.2" r="3.2" />
    </Svg>
  )
}

export function PlusIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M12 5v14M5 12h14" />
    </Svg>
  )
}

export function SofaIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M5 11V8.5A2.5 2.5 0 0 1 7.5 6h9A2.5 2.5 0 0 1 19 8.5V11" />
      <path d="M3 12.5a1.5 1.5 0 0 1 3 0V14h12v-1.5a1.5 1.5 0 0 1 3 0V18H3z" />
      <path d="M5.5 18v2M18.5 18v2" />
    </Svg>
  )
}

export function PlantIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M8 14h8l-1 6H9z" />
      <path d="M12 14V9.5" />
      <path d="M12 10c0-3 2-5 5-5 0 3-2 5-5 5zM12 11.5c0-2.5-1.7-4-4-4 0 2.5 1.7 4 4 4z" />
    </Svg>
  )
}

export function CapIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M5 15a7 7 0 0 1 14 0z" />
      <path d="M19 15h3M12 8V6.5" />
    </Svg>
  )
}

export function ShirtIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M8 4 4 6.5 5.5 10 7.5 9V20h9V9l2 1L20 6.5 16 4c-.5 1.5-2 2.5-4 2.5S8.5 5.5 8 4z" />
    </Svg>
  )
}

export function GlassesIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <circle cx="7" cy="14" r="3.2" />
      <circle cx="17" cy="14" r="3.2" />
      <path d="M10.2 13.5c1.2-.8 2.4-.8 3.6 0M3.8 13 3 9.5M20.2 13 21 9.5" />
    </Svg>
  )
}

export function TrophyIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M8 4h8v5a4 4 0 0 1-8 0z" />
      <path d="M8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4" />
      <path d="M12 13v4M9 20h6M10 17h4v3h-4z" />
    </Svg>
  )
}

export function LockIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <rect x="5" y="10.5" width="14" height="9.5" rx="2" />
      <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
    </Svg>
  )
}

export function BookIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M4 5.5C6.5 4.5 9.5 4.5 12 6c2.5-1.5 5.5-1.5 8-.5V19c-2.5-1-5.5-1-8 .5-2.5-1.5-5.5-1.5-8-.5z" />
      <path d="M12 6v13.5" />
    </Svg>
  )
}

export function LaptopIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <rect x="5" y="5.5" width="14" height="10" rx="1.5" />
      <path d="M3 18.5h18" />
    </Svg>
  )
}

export function MoonIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M19 14.5A7.5 7.5 0 0 1 9.5 5a7.5 7.5 0 1 0 9.5 9.5z" />
    </Svg>
  )
}

export function PencilIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="m15.5 4.5 4 4L9 19H5v-4z" />
      <path d="m13.5 6.5 4 4" />
    </Svg>
  )
}

export function CupIcon(props: IconProps) {
  return (
    <Svg {...props}>
      <path d="M5 9h11v5a5 5 0 0 1-5 5h-1a5 5 0 0 1-5-5z" />
      <path d="M16 10.5h1.5a2.5 2.5 0 0 1 0 5H16" />
      <path d="M9 3.5c-.6.8-.6 1.7 0 2.5M12 3.5c-.6.8-.6 1.7 0 2.5" />
    </Svg>
  )
}
