import { useQuery } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { Achievement } from '../../api/types'

export function useAchievements() {
  return useQuery({ queryKey: ['achievements'], queryFn: ({ signal }) => apiRequest<Achievement[]>('/achievements', { signal }) })
}
