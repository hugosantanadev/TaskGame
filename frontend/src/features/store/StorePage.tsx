import { useState } from 'react'
import { useSearchParams } from 'react-router'

import { asApiError } from '../../api/errors'
import type { InventoryItem, StoreItem, StoreItemCategory } from '../../api/types'
import { Button, ButtonLink } from '../../components/Button'
import { CheckIcon, CoinIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { Sheet } from '../../components/Sheet'
import { ToggleGroup } from '../../components/ToggleGroup'
import { itemKind, SLOT_LABEL } from '../../lib/collection'
import { plural } from '../../lib/days'
import { ItemSticker } from './ItemSticker'
import styles from './StorePage.module.css'
import {
  useCatalog,
  useEquip,
  useInventory,
  usePlaceItem,
  usePurchase,
  useRemoveItem,
  useUnequip,
  useWallet,
} from './storeApi'

type Filter = 'ALL' | StoreItemCategory

const VIEWS = [
  { value: 'store', label: 'Loja' },
  { value: 'collection', label: 'Minha coleção' },
] as const

const FILTERS: ReadonlyArray<{ value: Filter; label: string }> = [
  { value: 'ALL', label: 'Tudo' },
  { value: 'FURNITURE', label: 'Móveis' },
  { value: 'DECORATION', label: 'Decoração' },
  { value: 'CHARACTER', label: 'Personagem' },
]

export function StorePage() {
  const [params, setParams] = useSearchParams()
  const view = params.get('visao') === 'colecao' ? 'collection' : 'store'
  const wallet = useWallet()
  const balance = wallet.data?.balance ?? 0

  return (
    <div className={styles.page}>
      <PageTitle title={view === 'store' ? 'Loja' : 'Minha coleção'} />
      <div className={styles.top}>
        <h1 className={styles.heading}>Loja</h1>
        {wallet.data && (
          <p className={styles.balance}>
            <CoinIcon /> {balance}
            <span className="visually-hidden"> moedas</span>
          </p>
        )}
      </div>
      <ToggleGroup
        label="O que ver"
        value={view}
        options={VIEWS}
        onChange={(next) => setParams(next === 'collection' ? { visao: 'colecao' } : {})}
      />
      {view === 'store' ? <Catalog balance={balance} /> : <Collection />}
    </div>
  )
}

function Catalog({ balance }: { balance: number }) {
  const [filter, setFilter] = useState<Filter>('ALL')
  const catalog = useCatalog(filter === 'ALL' ? null : filter)
  const [buying, setBuying] = useState<StoreItem | null>(null)

  return (
    <>
      <p className={styles.note}>Moedas vêm das tarefas concluídas. Cada item é único: depois de comprado, é seu para sempre.</p>
      <ToggleGroup label="Categoria" value={filter} options={FILTERS} onChange={setFilter} />
      {catalog.isPending && <p className={styles.note}>Carregando a loja…</p>}
      {catalog.isError && (
        <p className={styles.error} role="alert">
          {asApiError(catalog.error).message}
        </p>
      )}
      <ul className={styles.grid}>
        {catalog.data?.map((item) => (
          <li key={item.id}>
            <StoreItemCard item={item} balance={balance} onBuy={() => setBuying(item)} />
          </li>
        ))}
      </ul>
      <Sheet open={buying !== null} title={buying ? `Comprar ${buying.name}?` : ''} onClose={() => setBuying(null)}>
        {buying && <PurchaseConfirm key={buying.id} item={buying} balance={balance} onDone={() => setBuying(null)} />}
      </Sheet>
    </>
  )
}

/** Cartão do catálogo: só apresenta; a compra é decidida por quem o usa. */
function StoreItemCard({ item, balance, onBuy }: { item: StoreItem; balance: number; onBuy: () => void }) {
  const missing = item.price - balance
  return (
    <article className={styles.card} data-owned={item.owned || undefined}>
      <ItemSticker item={item} />
      <h2 className={styles.name}>{item.name}</h2>
      <p className={styles.kind}>{itemKind(item)}</p>
      <p className={styles.description}>{item.description}</p>
      <div className={styles.footer}>
        <span className={styles.price}>
          <CoinIcon /> {item.price}
          <span className="visually-hidden"> moedas</span>
        </span>
        {item.owned ? (
          <span className={styles.owned}>
            <CheckIcon /> Na coleção
          </span>
        ) : missing > 0 ? (
          <span className={styles.missing}>Faltam {missing}</span>
        ) : (
          <Button className={styles.buy} onClick={onBuy} aria-label={`Comprar ${item.name}`}>
            Comprar
          </Button>
        )}
      </div>
    </article>
  )
}

function PurchaseConfirm({ item, balance, onDone }: { item: StoreItem; balance: number; onDone: () => void }) {
  const purchase = usePurchase()
  return (
    <div className={styles.confirm}>
      <div className={styles.confirmItem}>
        <ItemSticker item={item} size="l" />
        <p>{item.description}</p>
      </div>
      <p className={styles.note}>
        Custa {plural(item.price, 'moeda', 'moedas')}. Depois da compra, sobram {plural(balance - item.price, 'moeda', 'moedas')}.
      </p>
      {purchase.error && (
        <p className={styles.error} role="alert">
          {asApiError(purchase.error).message}
        </p>
      )}
      <Button block disabled={purchase.isPending} onClick={() => purchase.mutate(item, { onSuccess: onDone })}>
        <CoinIcon /> Comprar por {item.price}
      </Button>
    </div>
  )
}

function Collection() {
  const inventory = useInventory()
  if (inventory.isPending) return <p className={styles.note}>Carregando sua coleção…</p>
  if (inventory.isError) {
    return (
      <p className={styles.error} role="alert">
        {asApiError(inventory.error).message}
      </p>
    )
  }
  if (inventory.data.length === 0) {
    return (
      <div className={styles.empty}>
        <p className={styles.emptyTitle}>Sua coleção está vazia.</p>
        <p className={styles.note}>Conclua tarefas, junte moedas e escolha algo na loja. A caneca custa só 5.</p>
      </div>
    )
  }
  return (
    <>
      <ul className={styles.collection}>
        {inventory.data.map((owned) => (
          <li key={owned.id}>
            <CollectionItem owned={owned} />
          </li>
        ))}
      </ul>
      <div className={styles.links}>
        <ButtonLink to="/perfil/quarto" variant="secondary">
          Ver o quarto
        </ButtonLink>
        <ButtonLink to="/perfil/personagem" variant="secondary">
          Ver o personagem
        </ButtonLink>
      </div>
    </>
  )
}

function CollectionItem({ owned }: { owned: InventoryItem }) {
  const place = usePlaceItem()
  const remove = useRemoveItem()
  const equip = useEquip()
  const unequip = useUnequip()
  const busy = place.isPending || remove.isPending || equip.isPending || unequip.isPending
  const error = place.error ?? remove.error ?? equip.error ?? unequip.error
  const { item } = owned
  const where = owned.inRoom
    ? 'No quarto'
    : owned.equippedSlot
      ? `Vestido: ${SLOT_LABEL[owned.equippedSlot].toLowerCase()}`
      : 'Guardado'

  return (
    <article className={styles.ownedCard}>
      <ItemSticker item={item} />
      <div className={styles.ownedText}>
        <h2 className={styles.name}>{item.name}</h2>
        <p className={styles.kind}>
          {itemKind(item)}. {where}.
        </p>
        {error && (
          <p className={styles.error} role="alert">
            {asApiError(error).message}
          </p>
        )}
      </div>
      {item.slot === null ? (
        <Button
          variant={owned.inRoom ? 'secondary' : 'primary'}
          disabled={busy}
          onClick={() => (owned.inRoom ? remove.mutate(owned.id) : place.mutate(owned.id))}
          aria-label={`${owned.inRoom ? 'Tirar do quarto' : 'Colocar no quarto'}: ${item.name}`}
        >
          {owned.inRoom ? 'Tirar' : 'Colocar'}
        </Button>
      ) : (
        <Button
          variant={owned.equippedSlot ? 'secondary' : 'primary'}
          disabled={busy}
          onClick={() => {
            if (!item.slot) return
            if (owned.equippedSlot) unequip.mutate(item.slot)
            else equip.mutate({ slot: item.slot, inventoryItemId: owned.id })
          }}
          aria-label={`${owned.equippedSlot ? 'Tirar' : 'Vestir'}: ${item.name}`}
        >
          {owned.equippedSlot ? 'Tirar' : 'Vestir'}
        </Button>
      )}
    </article>
  )
}
