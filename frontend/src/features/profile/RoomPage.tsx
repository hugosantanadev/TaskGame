import { asApiError } from '../../api/errors'
import { Button, ButtonLink } from '../../components/Button'
import { PageTitle } from '../../components/PageTitle'
import { itemKind } from '../../lib/collection'
import { ItemSticker } from '../store/ItemSticker'
import { useRemoveItem, useRoom } from '../store/storeApi'
import styles from './Collection.module.css'

/** O quarto, por enquanto como lista (RF19). A ilustração futura vai consumir estes mesmos itens. */
export function RoomPage() {
  const room = useRoom()
  const remove = useRemoveItem()

  return (
    <div className={styles.page}>
      <PageTitle title="Quarto" />
      <ButtonLink to="/perfil" variant="quiet" className={styles.back}>
        Voltar ao perfil
      </ButtonLink>
      <h1 className={styles.heading}>Seu quarto</h1>
      <p className={styles.note}>Os móveis e as decorações que você colocou aqui. Por enquanto o quarto é uma lista; a ilustração vem depois.</p>
      {room.isPending && <p className={styles.note}>Carregando…</p>}
      {(room.error ?? remove.error) && (
        <p className={styles.error} role="alert">
          {asApiError(room.error ?? remove.error).message}
        </p>
      )}
      {room.data && room.data.items.length === 0 && (
        <div className={styles.empty}>
          <p className={styles.emptyTitle}>O quarto está vazio.</p>
          <p className={styles.note}>Compre um móvel ou uma decoração na loja e coloque aqui.</p>
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
      <ButtonLink to="/loja?visao=colecao" variant="secondary" block>
        Escolher na minha coleção
      </ButtonLink>
    </div>
  )
}
