import { describe, expect, it } from 'vitest'

import { niceStep } from './scale'

describe('passo do eixo do gráfico', () => {
  it('é sempre um número redondo que cobre o valor', () => {
    expect(niceStep(0.5)).toBe(1)
    expect(niceStep(1)).toBe(1)
    expect(niceStep(4.5)).toBe(5)
    expect(niceStep(7)).toBe(8)
    expect(niceStep(9)).toBe(10)
    expect(niceStep(22.5)).toBe(30)
    expect(niceStep(41)).toBe(50)
  })
})
