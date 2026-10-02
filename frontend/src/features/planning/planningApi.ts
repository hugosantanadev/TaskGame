import { useMutation, useQuery } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { ExtraInput, Occurrence, Week } from '../../api/types'
import { useRefreshPlan } from '../today/todayApi'

export type WhichWeek = 'current' | 'next'

export function useWeek(which: WhichWeek) {
  return useQuery({
    queryKey: ['week', which],
    queryFn: ({ signal }) => apiRequest<Week>(`/weeks/${which}`, { signal }),
  })
}

export function useAddOccurrence() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: ({ weekStart, ...body }: { weekStart: string; taskId: string; date: string; time: string | null }) =>
      apiRequest<Occurrence>(`/weeks/${weekStart}/occurrences`, { method: 'POST', body }),
    onSuccess: refresh,
  })
}

export function useMoveOccurrence() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: ({ id, ...body }: { id: string; date: string; time: string | null }) =>
      apiRequest<Occurrence>(`/occurrences/${id}`, { method: 'PATCH', body }),
    onSuccess: refresh,
  })
}

export function useRemoveOccurrence() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: (id: string) => apiRequest<void>(`/occurrences/${id}`, { method: 'DELETE' }),
    onSuccess: refresh,
  })
}

export function useCreateExtra() {
  const refresh = useRefreshPlan()
  return useMutation({
    mutationFn: (input: ExtraInput) => apiRequest<Occurrence>('/extras', { method: 'POST', body: input }),
    onSuccess: refresh,
  })
}
