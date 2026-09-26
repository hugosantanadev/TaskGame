import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
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
    },
  })
}
