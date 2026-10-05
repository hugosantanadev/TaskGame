import { useState, type CSSProperties } from 'react'

import { asApiError } from '../../api/errors'
import type { EquipmentStatus } from '../../api/types'
import { Button } from '../../components/Button'
import { CheckIcon } from '../../components/gameIcons'
import { Sheet } from '../../components/Sheet'
import { EQUIPMENT_ART } from '../../game/pixel/art/equipment'
import { PixelIcon, PixelSprite } from '../../game/pixel/PixelSprite'
import { categoryColor, categoryLabel } from '../../lib/categories'
import { plural } from '../../lib/days'
import { STARTER_LABEL, TRACK_LABEL, TRACK_ORDER, bonusText } from '../../lib/equipment'
import styles from './EquipmentList.module.css'
import { ItemSticker } from './ItemSticker'
import { useEquipment, usePurchase } from './storeApi'

/**
 * Melhorias do quarto: uma linha por trilha, com o que você tem agora, o bônus e o próximo degrau. Comprar
 * troca o móvel do quarto e aumenta as moedas das tarefas daquela categoria.
 */
export function EquipmentList({ balance }: { balance: number }) {
  const equipment = useEquipment()
  const [upgrading, setUpgrading] = useState<EquipmentStatus | null>(null)

  if (equipment.isPending) return <p className={styles.note}>Carregando as melhorias…</p>
  if (equipment.isError) {
    return (
      <p className={styles.error} role="alert">
        {asApiError(equipment.error).message}
      </p>
    )
  }
  const tracks = [...equipment.data].sort((a, b) => TRACK_ORDER.indexOf(a.track) - TRACK_ORDER.indexOf(b.track))

  return (
    <>
      <p className={styles.note}>Cada melhoria muda o quarto e rende moedas a mais nas tarefas da categoria dela.</p>
      <ul className={styles.list}>
        {tracks.map((status) => (
          <li key={status.track}>
            <TrackRow status={status} balance={balance} onUpgrade={() => setUpgrading(status)} />
          </li>
        ))}
      </ul>
      <Sheet
        open={upgrading !== null}
        title={upgrading?.next ? `Comprar ${upgrading.next.name}?` : ''}
        onClose={() => setUpgrading(null)}
      >
        {upgrading && <UpgradeConfirm key={upgrading.track} status={upgrading} balance={balance} onDone={() => setUpgrading(null)} />}
      </Sheet>
    </>
  )
}

function TrackRow({ status, balance, onUpgrade }: { status: EquipmentStatus; balance: number; onUpgrade: () => void }) {
  const art = EQUIPMENT_ART[status.track][status.tier]
  const missing = status.next ? status.next.price - balance : 0
  const [category] = status.categories

  return (
    <article className={styles.track} style={{ '--tape': categoryColor(category) } as CSSProperties}>
      <span className={styles.art} aria-hidden="true">
        {art ? <PixelSprite sprite={art} className={styles.sprite} /> : <span className={styles.empty}>?</span>}
      </span>
      <div className={styles.text}>
        <h2 className={styles.name}>
          {TRACK_LABEL[status.track]}
          <span className={styles.category}>{status.categories.map(categoryLabel).join(' e ')}</span>
        </h2>
        <p className={styles.current}>{status.current?.name ?? STARTER_LABEL[status.track]}</p>
        <p className={styles.bonus}>{status.coinBonus > 0 ? bonusText(status.coinBonus, status.categories) : 'Sem bônus ainda'}</p>
        <ol className={styles.steps} aria-label={`Degrau ${status.tier} de 3`}>
          {[1, 2, 3].map((step) => (
            <li key={step} data-on={step <= status.tier || undefined} />
          ))}
        </ol>
      </div>
      {status.next ? (
        <Button
          variant={missing > 0 ? 'secondary' : 'primary'}
          className={styles.upgrade}
          onClick={onUpgrade}
          aria-label={`Melhorar ${TRACK_LABEL[status.track]}: ${status.next.name} por ${status.next.price} moedas`}
        >
          <PixelIcon name="coin" /> {status.next.price}
        </Button>
      ) : (
        <span className={styles.done}>
          <CheckIcon aria-hidden="true" /> Completa
        </span>
      )}
    </article>
  )
}

function UpgradeConfirm({ status, balance, onDone }: { status: EquipmentStatus; balance: number; onDone: () => void }) {
  const purchase = usePurchase()
  const next = status.next
  if (!next) return null
  const missing = next.price - balance

  return (
    <div className={styles.confirm}>
      <div className={styles.confirmItem}>
        <ItemSticker item={next} size="l" />
        <div className={styles.confirmText}>
          <p>{next.description}</p>
          <p className={styles.confirmBonus}>{bonusText(status.nextBonus ?? 0, status.categories)}</p>
        </div>
      </div>
      {missing > 0 ? (
        <p className={styles.note}>
          Faltam {plural(missing, 'moeda', 'moedas')}. Conclua tarefas e volte aqui.
        </p>
      ) : (
        <p className={styles.note}>
          Custa {plural(next.price, 'moeda', 'moedas')}. Depois da compra, sobram {plural(balance - next.price, 'moeda', 'moedas')}.
        </p>
      )}
      {purchase.error && (
        <p className={styles.error} role="alert">
          {asApiError(purchase.error).message}
        </p>
      )}
      <Button block disabled={missing > 0 || purchase.isPending} onClick={() => purchase.mutate(next, { onSuccess: onDone })}>
        <PixelIcon name="coin" /> Comprar por {next.price}
      </Button>
    </div>
  )
}
