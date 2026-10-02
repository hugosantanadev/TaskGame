import { describe, expect, it } from 'vitest'

import { hourIn, safeTimeZone, timeOfDay } from './datetime'

describe('período do dia', () => {
  it('segue as faixas do backend (RN04)', () => {
    expect(timeOfDay(4)).toBe('NIGHT')
    expect(timeOfDay(5)).toBe('MORNING')
    expect(timeOfDay(11)).toBe('MORNING')
    expect(timeOfDay(12)).toBe('AFTERNOON')
    expect(timeOfDay(17)).toBe('SUNSET')
    expect(timeOfDay(19)).toBe('NIGHT')
  })

  it('a hora é a do fuso pedido', () => {
    expect(hourIn('America/Sao_Paulo', new Date('2026-10-02T12:00:00Z'))).toBe(9)
  })

  it('fuso desconhecido cai no do aparelho em vez de quebrar a tela', () => {
    expect(safeTimeZone('America/Recife')).toBe('America/Recife')
    expect(safeTimeZone('Fuso/Inventado')).not.toBe('Fuso/Inventado')
  })
})
