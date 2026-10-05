import type { OpenedChest, RankRef, StoreItem, TitleCode } from '../api/types'
import { compareRanks } from '../lib/rank'
import type { RewardEvent } from './rewardFeedback'

/** Os momentos grandes, que merecem a tela inteira; o resto fica no aviso pequeno (RewardToast). */
export type Moment =
  | { kind: 'title'; code: TitleCode }
  | { kind: 'rank'; rank: RankRef; items: StoreItem[] }
  | { kind: 'chest'; result: OpenedChest }

/** Título novo, subida de elo e baú aberto viram comemoração; queda de elo e o resto, não. */
export function momentsOf(event: RewardEvent): Moment[] {
  if (event.kind === 'completed') {
    const { attribute, xp } = event.result
    const moments: Moment[] = attribute.unlockedTitles.map((code) => ({ kind: 'title', code }))
    if (xp.promoted) moments.push({ kind: 'rank', rank: xp.status, items: xp.unlockedItems })
    return moments
  }
  if (event.kind === 'rank' && compareRanks(event.to, event.from) > 0) return [{ kind: 'rank', rank: event.to, items: [] }]
  if (event.kind === 'chest') return [{ kind: 'chest', result: event.result }]
  return []
}
