import { asApiError } from '../../api/errors'
import { useCurrentUser } from '../../auth/context'
import { Button, ButtonLink } from '../../components/Button'
import { PageTitle } from '../../components/PageTitle'
import { wornCodes } from '../../game/pixel/items'
import { itemKind } from '../../lib/collection'
import { hourIn, safeTimeZone, timeOfDay } from '../../lib/datetime'
import { useNow } from '../../lib/useNow'
import { ItemSticker } from '../store/ItemSticker'
import { tiersOf } from '../../lib/equipment'
import { useCharacter, useEquipment, useRemoveItem, useRoom } from '../store/storeApi'
import styles from './Collection.module.css'
import { RoomScene } from './RoomScene'

/** O quarto desenhado (RF19) e, embaixo, a lista do que está nele para tirar. */
export function RoomPage() {
  const room = useRoom()
  const remove = useRemoveItem()
  const character = useCharacter()
  const equipment = useEquipment()
  const user = useCurrentUser()
  const period = timeOfDay(hourIn(safeTimeZone(user.timeZone), useNow()))

  return (
    <div className={styles.page}>
      <PageTitle title="Quarto" />
      <ButtonLink to="/perfil" variant="quiet" className={styles.back}>
        Voltar ao perfil
      </ButtonLink>
      <h1 className={styles.heading}>Seu quarto</h1>
      {room.data && (
        <RoomScene
          equipment={tiersOf(equipment.data)}
          items={room.data.items.map((placed) => placed.item.code)}
          wearing={wornCodes(character.data)}
          sleeping={character.data?.state === 'SLEEPING'}
          period={period}
        />
      )}
      <p className={styles.note}>
        As melhorias (cama, mesa, computador…) aparecem sozinhas. Aqui você escolhe a decoração que fica no quarto.
      </p>
      {room.isPending && <p className={styles.note}>Carregando…</p>}
      {(room.error ?? remove.error) && (
        <p className={styles.error} role="alert">
          {asApiError(room.error ?? remove.error).message}
        </p>
      )}
      {room.data && room.data.items.length === 0 && (
        <div className={styles.empty}>
          <p className={styles.emptyTitle}>Nenhuma decoração ainda.</p>
          <p className={styles.note}>Que tal começar por uma caneca de café? Custa só 5 moedas.</p>
        </div>
      )}
      {room.data && room.data.items.length > 0 && (
        <ul className={styles.list}>
          {room.data.items.map((placed) => (
            <li key={placed.id}>
              <article className={styles.row}>
                <ItemSticker item={placed.item} />
                <div className={styles.rowText}>
                  <h2 className={styles.rowTitle}>{placed.item.name}</h2>
                  <p className={styles.rowMeta}>{itemKind(placed.item)}</p>
                </div>
                <Button variant="quiet" disabled={remove.isPending} onClick={() => remove.mutate(placed.id)} aria-label={`Tirar do quarto: ${placed.item.name}`}>
                  Tirar
                </Button>
              </article>
            </li>
          ))}
        </ul>
      )}
      <div className={styles.actionsRow}>
        <ButtonLink to="/loja" variant="secondary">
          Melhorar o quarto
        </ButtonLink>
        <ButtonLink to="/loja?visao=colecao" variant="secondary">
          Minha coleção
        </ButtonLink>
      </div>
    </div>
  )
}
