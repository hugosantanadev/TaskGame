import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { CompletionResult, Occurrence, ProofAttached, Today } from '../../api/types'
import { publishReward } from '../../game/rewardFeedback'

export const todayKey = ['today'] as const

export function useToday() {
  return useQuery({
    queryKey: todayKey,
    queryFn: ({ signal }) => apiRequest<Today>('/today', { signal }),
    refetchInterval: 5 * 60_000,
  })
}

function proofForm(photo: File | undefined): FormData | undefined {
  if (!photo) return undefined
  const form = new FormData()
  form.append('proof', photo)
  return form
}

/** Depois de concluir ou mudar o plano, Hoje e Semana precisam ser recarregados. */
export function useRefreshPlan() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: todayKey })
    void queryClient.invalidateQueries({ queryKey: ['week'] })
    void queryClient.invalidateQueries({ queryKey: ['wallet'] })
    void queryClient.invalidateQueries({ queryKey: ['achievements'] })
    void queryClient.invalidateQueries({ queryKey: ['character'] })
  }
}

export function useCompleteOccurrence() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: ({ occurrence, photo }: { occurrence: Occurrence; photo?: File }) =>
      apiRequest<CompletionResult>(`/occurrences/${occurrence.id}/complete`, { method: 'POST', body: proofForm(photo) }),
    onSuccess: (result, { occurrence }) => {
      publishReward({ kind: 'completed', title: occurrence.title, result })
      refresh()
    },
  })
}

export function useAttachProof() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: ({ occurrence, photo }: { occurrence: Occurrence; photo: File }) =>
      apiRequest<ProofAttached>(`/occurrences/${occurrence.id}/proof`, { method: 'POST', body: proofForm(photo) }),
    onSuccess: (result, { occurrence }) => {
      publishReward({ kind: 'proof', title: occurrence.title, result })
      refresh()
    },
  })
}

/** A foto vem com o token no cabeçalho, então não dá para usar a URL direto num <img>: vira um Blob. */
export function useProofImage(occurrenceId: string | null) {
  return useQuery({
    queryKey: ['proof', occurrenceId],
    queryFn: ({ signal }) => apiRequest<Blob>(`/occurrences/${occurrenceId}/proof`, { signal, responseType: 'blob' }),
    enabled: occurrenceId !== null,
    staleTime: Infinity,
  })
}
