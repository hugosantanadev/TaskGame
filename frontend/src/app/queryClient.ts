import { QueryClient } from '@tanstack/react-query'

import { ApiError } from '../api/errors'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // Erro 4xx não muda com nova tentativa; falha de rede e 5xx tentam mais duas vezes.
      retry: (failureCount, error) =>
        failureCount < 2 && !(error instanceof ApiError && error.status >= 400 && error.status < 500),
    },
    mutations: { retry: false },
  },
})
