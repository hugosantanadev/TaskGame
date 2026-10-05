import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest, endSessionLocally } from '../../api/client'
import type { User } from '../../api/types'
import { useAuth } from '../../auth/context'

export type ProfileChanges = Partial<Pick<User, 'displayName' | 'timeZone' | 'rankingVisible'>>

const profileKey = ['me'] as const

/** Hooks de dados do perfil: as telas não falam com a API diretamente. */
export function useProfile(initial: User) {
  return useQuery({
    queryKey: profileKey,
    queryFn: ({ signal }) => apiRequest<User>('/me', { signal }),
    initialData: initial,
  })
}

export function useUpdateProfile() {
  const queryClient = useQueryClient()
  const { updateUser } = useAuth()
  return useMutation({
    mutationFn: (changes: ProfileChanges) => apiRequest<User>('/me', { method: 'PATCH', body: changes }),
    onSuccess: (user) => {
      queryClient.setQueryData(profileKey, user)
      updateUser(user)
      // Aparecer ou não no ranking muda a lista
      void queryClient.invalidateQueries({ queryKey: ['ranking'] })
    },
  })
}

/** Baixa tudo o que a conta guarda (LGPD: portabilidade) como um arquivo JSON. */
export function useExportData() {
  return useMutation({
    mutationFn: () => apiRequest<Blob>('/me/export', { responseType: 'blob' }),
    onSuccess: (blob) => {
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `gasmtask-dados-${new Date().toISOString().slice(0, 10)}.json`
      link.click()
      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    },
  })
}

/** Exclui a conta de vez (confirma com a senha) e encerra a sessão neste aparelho. */
export function useDeleteAccount() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (password: string) => apiRequest<void>('/me', { method: 'DELETE', body: { password } }),
    onSuccess: () => {
      queryClient.clear()
      endSessionLocally()
    },
  })
}
