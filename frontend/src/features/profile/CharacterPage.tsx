import type { CSSProperties } from 'react'

import type { AttributeStatus, CharacterState } from '../../api/types'
import { asApiError } from '../../api/errors'
import { Button, ButtonLink } from '../../components/Button'
import { BookIcon, CupIcon, LaptopIcon, MoonIcon, PencilIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { ATTRIBUTE_LABELS, attributeColor, levelProgress, trainedBy } from '../../lib/attributes'
import { SLOT_LABEL, STATE_LABEL } from '../../lib/collection'
import { ItemSticker } from '../store/ItemSticker'
import { useCharacter, useUnequip } from '../store/storeApi'
import styles from './Collection.module.css'

const STATE_ICON: Record<CharacterState, typeof CupIcon> = {
  IDLE: CupIcon,
  STUDYING: PencilIcon,
  AT_COMPUTER: LaptopIcon,
  READING: BookIcon,
  SLEEPING: MoonIcon,
}

export function CharacterPage() {
  const character = useCharacter()
  const unequip = useUnequip()
  const state = character.data?.state ?? 'IDLE'
  const StateIcon = STATE_ICON[state]

  return (
    <div className={styles.page}>
      <PageTitle title="Personagem" />
      <ButtonLink to="/perfil" variant="quiet" className={styles.back}>
        Voltar ao perfil
      </ButtonLink>
      <h1 className={styles.heading}>Personagem</h1>
      {character.isPending && <p className={styles.note}>Carregando…</p>}
      {(character.error ?? unequip.error) && (
        <p className={styles.error} role="alert">
          {asApiError(character.error ?? unequip.error).message}
        </p>
      )}
      {character.data && (
        <>
          <section className={styles.state} aria-labelledby="estado">
            <span className={styles.stateIcon} aria-hidden="true">
              <StateIcon />
            </span>
            <div>
              <h2 id="estado" className={styles.stateTitle}>
                Agora: {STATE_LABEL[state].toLowerCase()}
              </h2>
              <p className={styles.note}>Muda sozinho com a tarefa em andamento: estudo, projeto, leitura ou sono.</p>
            </div>
          </section>
          <section className={styles.sheet} aria-labelledby="ficha">
            <h2 id="ficha" className={styles.sheetTitle}>
              Ficha
            </h2>
            <p className={styles.note}>Cada categoria de tarefa treina um atributo. Treino só soma.</p>
            <ul className={styles.attributes}>
              {character.data.attributes.map((status) => (
                <li key={status.attribute}>
                  <AttributeRow status={status} />
                </li>
              ))}
            </ul>
          </section>
          <ul className={styles.list} aria-label="O que está vestindo">
            {character.data.slots.map(({ slot, item }) => (
              <li key={slot}>
                <article className={styles.row}>
                  {item ? <ItemSticker item={item.item} /> : <span className={styles.emptySlot} aria-hidden="true" />}
                  <div className={styles.rowText}>
                    <h2 className={styles.rowTitle}>{SLOT_LABEL[slot]}</h2>
                    <p className={styles.rowMeta}>{item ? item.item.name : 'Vazio'}</p>
                  </div>
                  {item && (
                    <Button variant="quiet" disabled={unequip.isPending} onClick={() => unequip.mutate(slot)} aria-label={`Tirar: ${item.item.name}`}>
                      Tirar
                    </Button>
                  )}
                </article>
              </li>
            ))}
          </ul>
        </>
      )}
      <ButtonLink to="/loja?visao=colecao" variant="secondary" block>
        Escolher na minha coleção
      </ButtonLink>
    </div>
  )
}

function AttributeRow({ status }: { status: AttributeStatus }) {
  const name = ATTRIBUTE_LABELS[status.attribute]
  return (
    <div className={styles.attribute} style={{ '--attribute': attributeColor(status) } as CSSProperties}>
      <span className={styles.attributeName}>{name}</span>
      <span className={styles.attributeLevel}>Nv {status.level}</span>
      <span
        className={styles.attributeBar}
        role="progressbar"
        aria-label={`${name}: nível ${status.level}`}
        aria-valuemin={status.levelStartXp}
        aria-valuemax={status.nextLevelXp}
        aria-valuenow={status.xp}
      >
        <span style={{ width: `${levelProgress(status) * 100}%` }} />
      </span>
      <span className={styles.attributeMeta}>
        {trainedBy(status)} · {status.xp}/{status.nextLevelXp} XP
      </span>
    </div>
  )
}
