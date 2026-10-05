import { useState } from 'react'
import { useSearchParams } from 'react-router'

import { asApiError } from '../../api/errors'
import type { InventoryItem, StoreItem, StoreItemCategory } from '../../api/types'
import { Button, ButtonLink } from '../../components/Button'
import { CheckIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { Sheet } from '../../components/Sheet'
import { ToggleGroup } from '../../components/ToggleGroup'
import { PixelIcon } from '../../game/pixel/PixelSprite'
import { itemKind, SLOT_LABEL } from '../../lib/collection'
import { plural } from '../../lib/days'
import { EquipmentList } from './EquipmentList'
import { ItemSticker } from './ItemSticker'
import styles from './StorePage.module.css'
import {
  useCatalog,
  useEquip,
  useEquipment,
  useInventory,
  usePlaceItem,
  usePurchase,
  useRemoveItem,
  useBuyFreeze,
  useStreak,
  useUnequip,
  useWallet,
} from './storeApi'

type Filter = 'ALL' | Exclude<StoreItemCategory, 'EQUIPMENT'>
type View = 'upgrades' | 'store' | 'collection'

/** As três visões da Loja: melhorar o quarto (o que dá bônus), comprar estilo e ver o que já é seu. */
const VIEWS: ReadonlyArray<{ value: View; label: string; param: string | null; title: string }> = [
  { value: 'upgrades', label: 'Melhorias', param: null, title: 'Melhorias do quarto' },
  { value: 'store', label: 'Itens', param: 'itens', title: 'Loja' },
  { value: 'collection', label: 'Coleção', param: 'colecao', title: 'Minha coleção' },
]

const FILTERS: ReadonlyArray<{ value: Filter; label: string }> = [
  { value: 'ALL', label: 'Tudo' },
  { value: 'DECORATION', label: 'Decoração' },
  { value: 'FURNITURE', label: 'Móveis' },
  { value: 'CHARACTER', label: 'Roupas' },
]

export function StorePage() {
  const [params, setParams] = useSearchParams()
  const view = VIEWS.find((option) => option.param === params.get('visao'))?.value ?? 'upgrades'
  const wallet = useWallet()
  const balance = wallet.data?.balance ?? 0

  return (
    <div className={styles.page}>
      <PageTitle title={VIEWS.find((option) => option.value === view)?.title ?? 'Loja'} />
      <div className={styles.top}>
        <h1 className={styles.heading}>Loja</h1>
        {wallet.data && (
          <p className={styles.balance}>
            <PixelIcon name="coin" /> {balance}
            <span className="visually-hidden"> moedas</span>
          </p>
        )}
      </div>
      <ToggleGroup
        label="O que ver"
        value={view}
        options={VIEWS}
        onChange={(next) => {
          const param = VIEWS.find((option) => option.value === next)?.param
          setParams(param ? { visao: param } : {})
        }}
      />
      {view === 'upgrades' && <EquipmentList balance={balance} />}
      {view === 'store' && <Catalog balance={balance} />}
      {view === 'collection' && <Collection />}
    </div>
  )
}

function Catalog({ balance }: { balance: number }) {
  const [filter, setFilter] = useState<Filter>('ALL')
  const catalog = useCatalog(filter === 'ALL' ? null : filter)
  const [buying, setBuying] = useState<StoreItem | null>(null)

  return (
    <>
      <FreezeCard balance={balance} />
      <p className={styles.note}>Moedas vêm das tarefas concluídas. Cada item é único: depois de comprado, é seu para sempre.</p>
      <ToggleGroup label="Categoria" value={filter} options={FILTERS} onChange={setFilter} />
      {catalog.isPending && <p className={styles.note}>Carregando a loja…</p>}
      {catalog.isError && (
        <p className={styles.error} role="alert">
          {asApiError(catalog.error).message}
        </p>
      )}
      <ul className={styles.grid}>
        {catalog.data?.filter((item) => item.category !== 'EQUIPMENT').map((item) => (
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

/** Protetor de sequência: o único item que se gasta. Fica guardado e é usado sozinho num dia de falha. */
function FreezeCard({ balance }: { balance: number }) {
  const streak = useStreak()
  const buy = useBuyFreeze()
  if (!streak.data) return null
  const { freezes, maxFreezes, freezePrice } = streak.data
  const full = freezes >= maxFreezes
  const missing = freezePrice - balance

  return (
    <section className={styles.freeze} aria-labelledby="freeze-title">
      <span className={styles.freezeIcon} aria-hidden="true">
        <PixelIcon name="shield" />
      </span>
      <div className={styles.freezeText}>
        <h2 id="freeze-title" className={styles.name}>
          Protetor de sequência
        </h2>
        <p className={styles.description}>
          Salva sua sequência num dia em que faltar uma obrigatória. É usado sozinho, na virada do dia.
        </p>
        <p className={styles.kind}>
          Você tem {freezes} de {maxFreezes}
        </p>
        {buy.error && (
          <p className={styles.error} role="alert">
            {asApiError(buy.error).message}
          </p>
        )}
      </div>
      <div className={styles.freezeBuy}>
        <span className={styles.price}>
          <PixelIcon name="coin" /> {freezePrice}
          <span className="visually-hidden"> moedas</span>
        </span>
        <Button
          variant="secondary"
          disabled={full || missing > 0 || buy.isPending}
          onClick={() => buy.mutate()}
          aria-label={`Comprar protetor de sequência por ${freezePrice} moedas`}
        >
          {full ? 'Cheio' : missing > 0 ? `Faltam ${missing}` : 'Comprar'}
        </Button>
      </div>
    </section>
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
          <PixelIcon name="coin" /> {item.price}
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
        <PixelIcon name="coin" /> Comprar por {item.price}
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
  const equipment = useEquipment()
  const place = usePlaceItem()
  const remove = useRemoveItem()
  const equip = useEquip()
  const unequip = useUnequip()
  const busy = place.isPending || remove.isPending || equip.isPending || unequip.isPending
  const error = place.error ?? remove.error ?? equip.error ?? unequip.error
  const { item } = owned
  const currentTier = item.track ? equipment.data?.find((status) => status.track === item.track)?.tier : undefined
  const where = item.track
    ? currentTier !== undefined && item.tier !== null && item.tier < currentTier
      ? 'Substituída por uma melhor'
      : 'No quarto'
    : owned.inRoom
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
      {item.track ? null : item.slot === null ? (
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
