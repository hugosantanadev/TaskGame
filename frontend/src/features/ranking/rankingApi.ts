import { keepPreviousData, useInfiniteQuery } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type { Ranking, RankingMetric } from '../../api/types'

const PAGE_SIZE = 20

/** Ranking da semana, de 20 em 20. Trocar de métrica mantém a lista anterior (esmaecida) até a nova chegar. */
export function useRanking(metric: RankingMetric) {
  return useInfiniteQuery({
    queryKey: ['ranking', metric],
    queryFn: ({ pageParam, signal }) =>
      apiRequest<Ranking>(`/rankings?metric=${metric}&page=${pageParam}&size=${PAGE_SIZE}`, { signal }),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.entries.page + 1 < last.entries.totalPages ? last.entries.page + 1 : undefined),
    placeholderData: keepPreviousData,
  })
}
