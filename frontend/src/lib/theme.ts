import { useState } from 'react'

/** Aparência do app: segue o sistema, ou fica sempre clara ou escura. Vale para este aparelho. */
export type ThemeChoice = 'auto' | 'light' | 'dark'

const KEY = 'gasmtask-theme'
const BAR_COLOR = { light: '#ffffff', dark: '#151726' }

export function readTheme(): ThemeChoice {
  try {
    const saved = localStorage.getItem(KEY)
    return saved === 'light' || saved === 'dark' ? saved : 'auto'
  } catch {
    return 'auto' // navegação privada ou armazenamento bloqueado: segue o sistema
  }
}

/** Marca o <html> (o CSS faz o resto) e ajusta a cor da barra do navegador no celular. */
export function applyTheme(choice: ThemeChoice): void {
  const root = document.documentElement
  if (choice === 'auto') delete root.dataset.theme
  else root.dataset.theme = choice
  const dark = choice === 'dark' || (choice === 'auto' && window.matchMedia?.('(prefers-color-scheme: dark)').matches)
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', dark ? BAR_COLOR.dark : BAR_COLOR.light)
}

export function saveTheme(choice: ThemeChoice): void {
  try {
    if (choice === 'auto') localStorage.removeItem(KEY)
    else localStorage.setItem(KEY, choice)
  } catch {
    // Sem armazenamento a escolha vale só até fechar o app
  }
  applyTheme(choice)
}

export function useTheme(): [ThemeChoice, (choice: ThemeChoice) => void] {
  const [theme, setTheme] = useState<ThemeChoice>(readTheme)
  return [
    theme,
    (choice) => {
      saveTheme(choice)
      setTheme(choice)
    },
  ]
}
