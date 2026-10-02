import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { DayOfWeek, Mission, MissionInput, ScheduleEntry } from '../../api/types'
import { formatTime, listJoin, shortDay, WEEK_DAYS } from '../../lib/days'
import { useRefreshPlan } from '../today/todayApi'

export function useMissions(archived: boolean) {
  return useQuery({
    queryKey: ['missions', archived],
    queryFn: ({ signal }) => apiRequest<Mission[]>(`/tasks?archived=${archived}`, { signal }),
  })
}

export function useMission(id: string | undefined) {
  return useQuery({
    queryKey: ['mission', id],
    queryFn: ({ signal }) => apiRequest<Mission>(`/tasks/${id}`, { signal }),
    enabled: Boolean(id),
  })
}

function useRefreshMissions() {
  const queryClient = useQueryClient()
  const refreshPlan = useRefreshPlan()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['missions'] })
    void queryClient.invalidateQueries({ queryKey: ['mission'] })
    refreshPlan()
  }
}

export function useSaveMission() {
  const refresh = useRefreshMissions()
  return useMutation({
    mutationFn: ({ id, input }: { id?: string; input: MissionInput }) =>
      id
        ? apiRequest<Mission>(`/tasks/${id}`, { method: 'PUT', body: input })
        : apiRequest<Mission>('/tasks', { method: 'POST', body: input }),
    onSuccess: refresh,
  })
}

export function useArchiveMission() {
  const refresh = useRefreshMissions()
  return useMutation({
    mutationFn: (id: string) => apiRequest<Mission>(`/tasks/${id}/archive`, { method: 'POST' }),
    onSuccess: refresh,
  })
}

export function fetchSuggestion(timesPerWeek: number) {
  return apiRequest<{ timesPerWeek: number; days: DayOfWeek[] }>(`/tasks/schedule-suggestion?timesPerWeek=${timesPerWeek}`)
}

/** "seg, qua e sex às 08:00", "todos os dias" ou, com horários diferentes, "seg 08:00, qua 09:30". */
export function scheduleSummary(schedule: ScheduleEntry[]): string {
  const ordered = WEEK_DAYS.map((day) => schedule.find((entry) => entry.dayOfWeek === day.id)).filter(
    (entry): entry is ScheduleEntry => entry !== undefined,
  )
  const times = new Set(ordered.map((entry) => formatTime(entry.time)))
  if (times.size > 1) {
    return ordered.map((entry) => `${shortDay(entry.dayOfWeek)} ${formatTime(entry.time) ?? 'sem horário'}`).join(', ')
  }
  const days = ordered.length === 7 ? 'todos os dias' : listJoin(ordered.map((entry) => shortDay(entry.dayOfWeek)))
  const time = formatTime(ordered[0]?.time)
  return time ? `${days} às ${time}` : days
}
