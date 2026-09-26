import type { ButtonHTMLAttributes } from 'react'

import styles from './Button.module.css'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'quiet'
  tone?: 'default' | 'danger'
  block?: boolean
}

export function Button({ variant = 'primary', tone = 'default', block = false, className, type = 'button', ...props }: ButtonProps) {
  const classes = [styles.button, styles[variant], tone === 'danger' && styles.danger, block && styles.block, className]
    .filter(Boolean)
    .join(' ')
  return <button type={type} className={classes} {...props} />
}
