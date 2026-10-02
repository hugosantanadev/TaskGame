import type { ButtonHTMLAttributes } from 'react'
import { Link, type LinkProps } from 'react-router'

import styles from './Button.module.css'

type Variant = 'primary' | 'secondary' | 'quiet'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant
  tone?: 'default' | 'danger'
  block?: boolean
}

function classesFor(variant: Variant, tone: 'default' | 'danger', block: boolean, className?: string) {
  return [styles.button, styles[variant], tone === 'danger' && styles.danger, block && styles.block, className]
    .filter(Boolean)
    .join(' ')
}

export function Button({ variant = 'primary', tone = 'default', block = false, className, type = 'button', ...props }: ButtonProps) {
  return <button type={type} className={classesFor(variant, tone, block, className)} {...props} />
}

/** Link com cara de botão, para navegar (ex.: "Nova missão"). */
export function ButtonLink({
  variant = 'primary',
  block = false,
  className,
  ...props
}: LinkProps & { variant?: Variant; block?: boolean }) {
  return <Link className={classesFor(variant, 'default', block, className)} {...props} />
}
