import { keepPreviousData, useQuery } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { MissionEvolution, MissionSummary, StatsGranularity, StatsHistory, StatsOverview, WeekSummary } from '../../api/types'

/** Tudo sob ['stats'] é invalidado quando o usuário conclui uma tarefa ou mexe no plano. */
const statsKey = ['stats'] as const

export function useStatsOverview() {
  return useQuery({
    queryKey: [...statsKey, 'overview'],
    queryFn: ({ signal }) => apiRequest<StatsOverview>('/stats/overview', { signal }),
  })
}

/** Ao trocar semanas por meses, o gráfico anterior fica na tela (esmaecido) até o novo chegar. */
export function useStatsHistory(granularity: StatsGranularity, periods: number) {
  return useQuery({
    queryKey: [...statsKey, 'history', granularity, periods],
    queryFn: ({ signal }) =>
      apiRequest<StatsHistory>(`/stats/history?granularity=${granularity}&periods=${periods}`, { signal }),
    placeholderData: keepPreviousData,
  })
}

export function useWeekSummary(weekStart: string) {
  return useQuery({
    queryKey: [...statsKey, 'week', weekStart],
    queryFn: ({ signal }) => apiRequest<WeekSummary>(`/weeks/${weekStart}/summary`, { signal }),
    placeholderData: keepPreviousData,
  })
}

export function useMissionStats() {
  return useQuery({
    queryKey: [...statsKey, 'missions'],
    queryFn: ({ signal }) => apiRequest<MissionSummary[]>('/stats/missions', { signal }),
  })
}

/** Evolução de uma missão; trocar de missão mantém o gráfico anterior até o novo chegar. */
export function useMissionEvolution(taskId: string | null, weeks = 12) {
  return useQuery({
    queryKey: [...statsKey, 'missions', taskId, weeks],
    queryFn: ({ signal }) => apiRequest<MissionEvolution>(`/stats/missions/${taskId}?weeks=${weeks}`, { signal }),
    enabled: taskId !== null,
    placeholderData: keepPreviousData,
  })
}
