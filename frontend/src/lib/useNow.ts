import { useEffect, useState } from 'react'

/** Data atual que se atualiza sozinha (a tela Hoje vira o dia e o período sem recarregar). */
export function useNow(intervalMs = 60_000): Date {
  const [now, setNow] = useState(() => new Date())
  useEffect(() => {
    const id = window.setInterval(() => setNow(new Date()), intervalMs)
    return () => window.clearInterval(id)
  }, [intervalMs])
  return now
}
