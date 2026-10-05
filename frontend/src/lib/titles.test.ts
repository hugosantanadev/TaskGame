import { describe, expect, it } from 'vitest'

import type { TitleStatus } from '../api/types'
import { bestTitle, isMasterTitle, nextTitle, titleLabel, titleSentence } from './titles'

const titles: TitleStatus[] = [
  { code: 'STUDENT', attribute: 'INTELLIGENCE', level: 3, unlocked: true },
  { code: 'SHARP_MIND', attribute: 'INTELLIGENCE', level: 6, unlocked: true },
  { code: 'STUDY_MASTER', attribute: 'INTELLIGENCE', level: 10, unlocked: false },
  { code: 'GYM_REGULAR', attribute: 'STRENGTH', level: 3, unlocked: false },
]

describe('títulos', () => {
  it('tem nome e frase de comemoração', () => {
    expect(titleLabel('STUDY_MASTER')).toBe('Mestre nos estudos')
    expect(titleSentence('GYM_RAT')).toBe('Você é Rato de academia')
    expect(isMasterTitle('STUDY_MASTER')).toBe(true)
    expect(isMasterTitle('GYM_RAT')).toBe(false)
  })

  it('acha o maior título ganho e o próximo a ganhar de cada atributo', () => {
    expect(bestTitle(titles, 'INTELLIGENCE')?.code).toBe('SHARP_MIND')
    expect(nextTitle(titles, 'INTELLIGENCE')?.code).toBe('STUDY_MASTER')
    expect(bestTitle(titles, 'STRENGTH')).toBeNull()
    expect(nextTitle(titles, 'STRENGTH')?.code).toBe('GYM_REGULAR')
  })
})
