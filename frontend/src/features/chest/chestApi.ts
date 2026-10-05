import { useMutation, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { Chest, OpenedChest } from '../../api/types'
import { useCurrentUser } from '../../auth/context'
import { writeSeenRank } from '../../game/rankMemory'
import { publishReward } from '../../game/rewardFeedback'

/** Abre o baú da semana: o resultado vira a comemoração e as telas de saldo, XP e coleção são recarregadas. */
export function useOpenChest() {
  const queryClient = useQueryClient()
  const user = useCurrentUser()
  return useMutation({
    mutationFn: (chest: Chest) => apiRequest<OpenedChest>(`/chests/${chest.id}/open`, { method: 'POST' }),
    onSuccess: (result) => {
      writeSeenRank(user.id, result.xp.status)
      publishReward({ kind: 'chest', result })
      for (const key of ['today', 'wallet', 'store', 'inventory', 'rank', 'ranking', 'stats', 'character']) {
        void queryClient.invalidateQueries({ queryKey: [key] })
      }
    },
  })
}
