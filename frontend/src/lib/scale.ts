const NICE_STEPS = [1, 2, 3, 4, 5, 6, 8, 10]

/** Passo "redondo" de eixo (1, 2, 5, 10, 20…) que cobre {@code raw}, nunca menor que 1: as contagens são inteiras. */
export function niceStep(raw: number): number {
  if (raw <= 1) return 1
  const magnitude = 10 ** Math.floor(Math.log10(raw))
  const step = NICE_STEPS.find((nice) => nice * magnitude >= raw) ?? 10
  return step * magnitude
}
