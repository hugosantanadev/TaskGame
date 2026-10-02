import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { ReminderSettings, UpcomingReminder } from '../../api/types'

const settingsKey = ['reminder-settings'] as const
/** Invalidado quando o plano muda (concluir tira a tarefa da lista) e quando as preferências mudam. */
const upcomingKey = ['reminders'] as const

export function useReminderSettings() {
  return useQuery({
    queryKey: settingsKey,
    queryFn: ({ signal }) => apiRequest<ReminderSettings>('/me/reminder-settings', { signal }),
  })
}

export function useUpdateReminderSettings() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (settings: ReminderSettings) =>
      apiRequest<ReminderSettings>('/me/reminder-settings', { method: 'PUT', body: settings }),
    onSuccess: (settings) => {
      queryClient.setQueryData(settingsKey, settings)
      void queryClient.invalidateQueries({ queryKey: upcomingKey })
    },
  })
}

/** Lembretes das próximas 24 horas, recalculados a cada 15 minutos enquanto o app está aberto. */
export function useUpcomingReminders() {
  return useQuery({
    queryKey: upcomingKey,
    queryFn: ({ signal }) => apiRequest<UpcomingReminder[]>('/reminders/upcoming?hours=24', { signal }),
    refetchInterval: 15 * 60_000,
  })
}
