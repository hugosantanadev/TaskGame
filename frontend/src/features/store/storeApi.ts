import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { apiRequest } from '../../api/client'
import type {
  CharacterSlot,
  CharacterView,
  InventoryItem,
  PurchaseResult,
  Room,
  StoreItem,
  StoreItemCategory,
  Wallet,
} from '../../api/types'
import { publishReward } from '../../game/rewardFeedback'

export function useWallet() {
  return useQuery({ queryKey: ['wallet'], queryFn: ({ signal }) => apiRequest<Wallet>('/wallet', { signal }) })
}

export function useCatalog(category: StoreItemCategory | null) {
  return useQuery({
    queryKey: ['store', category ?? 'ALL'],
    queryFn: ({ signal }) => apiRequest<StoreItem[]>(`/store/items${category ? `?category=${category}` : ''}`, { signal }),
  })
}

export function useInventory() {
  return useQuery({ queryKey: ['inventory'], queryFn: ({ signal }) => apiRequest<InventoryItem[]>('/inventory', { signal }) })
}

export function useRoom() {
  return useQuery({ queryKey: ['room'], queryFn: ({ signal }) => apiRequest<Room>('/room', { signal }) })
}

export function useCharacter() {
  return useQuery({ queryKey: ['character'], queryFn: ({ signal }) => apiRequest<CharacterView>('/character', { signal }) })
}

/** Toda mudança na coleção mexe em várias telas: loja (já tenho), inventário, quarto, personagem e saldo. */
function useRefreshCollection() {
  const queryClient = useQueryClient()
  return () => {
    for (const key of ['store', 'inventory', 'room', 'character', 'wallet', 'today']) {
      void queryClient.invalidateQueries({ queryKey: [key] })
    }
  }
}

export function usePurchase() {
  const refresh = useRefreshCollection()
  return useMutation({
    mutationFn: (item: StoreItem) => apiRequest<PurchaseResult>(`/store/items/${item.id}/purchase`, { method: 'POST' }),
    onSuccess: (result, item) => {
      publishReward({ kind: 'purchase', title: item.name, balance: result.walletBalance })
      refresh()
    },
  })
}

export function usePlaceItem() {
  const refresh = useRefreshCollection()
  return useMutation({
    mutationFn: (inventoryItemId: string) => apiRequest<Room>(`/room/items/${inventoryItemId}`, { method: 'PUT' }),
    onSuccess: refresh,
  })
}

export function useRemoveItem() {
  const refresh = useRefreshCollection()
  return useMutation({
    mutationFn: (inventoryItemId: string) => apiRequest<Room>(`/room/items/${inventoryItemId}`, { method: 'DELETE' }),
    onSuccess: refresh,
  })
}

export function useEquip() {
  const refresh = useRefreshCollection()
  return useMutation({
    mutationFn: ({ slot, inventoryItemId }: { slot: CharacterSlot; inventoryItemId: string }) =>
      apiRequest<CharacterView>(`/character/slots/${slot}`, { method: 'PUT', body: { inventoryItemId } }),
    onSuccess: refresh,
  })
}

export function useUnequip() {
  const refresh = useRefreshCollection()
  return useMutation({
    mutationFn: (slot: CharacterSlot) => apiRequest<CharacterView>(`/character/slots/${slot}`, { method: 'DELETE' }),
    onSuccess: refresh,
  })
}
